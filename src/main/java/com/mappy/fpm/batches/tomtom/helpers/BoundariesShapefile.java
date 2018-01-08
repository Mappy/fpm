package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.LongLineSplitter;
import com.vividsolutions.jts.geom.*;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.vividsolutions.jts.algorithm.Centroid.getCentroid;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

public abstract class BoundariesShapefile extends TomtomShapefile {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private final String osmLevel;
    private final Integer tomtomLevel;

    private final CapitalProvider capitalProvider;
    private final TownTagger townTagger;
    private final String zone;
    protected final OsmLevelGenerator osmLevelGenerator;
    protected final NameProvider nameProvider;

    protected BoundariesShapefile(String filename, int tomtomLevel, CapitalProvider capitalProvider, TownTagger townTagger, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator) {
        super(filename);
        this.capitalProvider = capitalProvider;
        this.townTagger = townTagger;
        String[] split = filename.split("/");
        zone = split[split.length - 1].split("_[_2]")[0];

        this.osmLevelGenerator = osmLevelGenerator;
        this.osmLevel = osmLevelGenerator.getOsmLevel(zone, tomtomLevel);
        this.tomtomLevel = tomtomLevel;
        this.nameProvider = nameProvider;
        if (new File(filename).exists()) {
            this.nameProvider.loadAlternateNames("an.dbf");
        }
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        Long extId = feature.getLong("ID");

        Map<String, String> relationTags = newHashMap();
        relationTags.putAll(nameProvider.getAlternateNames(extId));
        relationTags.put("ref:tomtom", String.valueOf(extId));

        ofNullable(feature.getString("ORDER0" + tomtomLevel)).ifPresent(alpha3 -> relationTags.put("ref:INSEE", getInseeWithAlpha3(alpha3)));
        ofNullable(feature.getLong("POP")).filter(pop -> pop > 0).ifPresent(pop -> relationTags.put("population", String.valueOf(pop)));

        List<RelationMember> members = newArrayList();
        String name = feature.getString("NAME");
        if (name != null) {
            Map<String, String> labelTags = newHashMap(relationTags);
            labelTags.put("name", name);
            MultiPolygon multiPolygon = feature.getMultiPolygon();

            getLabel(serializer, labelTags, multiPolygon).ifPresent(members::add);

            Map<String, String> wayTags = of("name", name, "boundary", "administrative", "admin_level", osmLevel);
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
                    for (Geometry geom : LongLineSplitter.split(polygon.getInteriorRingN(j), 100)) {
                        members.add(addRelationMember(serializer, wayTags, (LineString) geom, "inner"));
                    }
                }
                for (Geometry geom : LongLineSplitter.split(polygon.getExteriorRing(), 100)) {
                    members.add(addRelationMember(serializer, wayTags, (LineString) geom, "outer"));
                }
            }

            putRelationTags(relationTags, wayTags);

            getAdminCenter(serializer, feature).ifPresent(members::add);

            serializer.writeRelation(members, relationTags);
        }
    }

    private void putRelationTags(Map<String, String> tags, Map<String, String> wayTags) {
        tags.putAll(wayTags);
        tags.put("type", "boundary");
        tags.put("layer", osmLevel);
    }

    protected String getInseeWithAlpha3(String alpha3) {
        return alpha3;
    }

    private Optional<RelationMember> getAdminCenter(GeometrySerializer serializer, Feature feature) {
        if (tomtomLevel <= 7) {
            return getCapital(serializer, feature);
        } else if(tomtomLevel <= 9){
            return getTown(serializer, feature);
        } else {
            return empty();
        }
    }

    private Optional<RelationMember> getCapital(GeometrySerializer serializer, Feature feature) {

        Optional<Centroid> capital = capitalProvider.get(tomtomLevel).stream().filter(c -> feature.getGeometry().contains(c.getPoint())).findFirst();
        if (capital.isPresent()) {
            Centroid cityCenter = capital.get();
            Map<String, String> adminTags = newHashMap(of("name", cityCenter.getName()));
            cityCenter.getPlace().ifPresent(p -> adminTags.put("place", p));
            String capitalValue = osmLevelGenerator.getOsmLevel(zone, cityCenter.getAdminclass());
            adminTags.put("capital", "2".equals(capitalValue) ? "yes" : capitalValue);
            Optional<Node> node = serializer.writePoint(cityCenter.getPoint(), adminTags);
            return node.map(adminCenter -> new RelationMember(adminCenter.getId(), EntityType.Node, "admin_center"));
        } else {
            return empty();
        }
    }

    private Optional<RelationMember> getTown(GeometrySerializer serializer, Feature feature) {

        Centroid cityCenter = townTagger.get(feature.getLong("CITYCENTER"));

        if (cityCenter != null) {
            Map<String, String> tags = newHashMap();
            tags.put("name", cityCenter.getName());
            cityCenter.getPlace().ifPresent(p -> tags.put("place", p));
            ofNullable(cityCenter.getPostcode()).ifPresent(code -> tags.put("addr:postcode", code));

            String capital = osmLevelGenerator.getOsmLevel(zone, cityCenter.getAdminclass());
            tags.put("capital", "2".equals(capital) ? "yes" : capital);

            tags.putAll(nameProvider.getAlternateCityNames(cityCenter.getId()));

            Optional<Node> node = serializer.writePoint(cityCenter.getPoint(), tags);
            return node.map(adminCenter -> new RelationMember(adminCenter.getId(), EntityType.Node, "admin_center"));
        } else {
            return empty();
        }
    }

    private RelationMember addRelationMember(GeometrySerializer serializer, Map<String, String> wayTags, LineString geom, String memberRole) {
        Long wayId = serializer.writeBoundary(geom, wayTags);
        return new RelationMember(wayId, Way, memberRole);
    }

    private Optional<RelationMember> getLabel(GeometrySerializer serializer, Map<String, String> tags, MultiPolygon multiPolygon) {
        Optional<Node> node = serializer.writePoint(GEOMETRY_FACTORY.createPoint(getCentroid(multiPolygon)), tags);
        return node.map(n -> new RelationMember(n.getId(), Node, "label"));
    }
}

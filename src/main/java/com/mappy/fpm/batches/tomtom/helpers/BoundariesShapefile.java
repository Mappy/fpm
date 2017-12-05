package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.LongLineSplitter;
import com.neovisionaries.i18n.CountryCode;
import com.vividsolutions.jts.algorithm.Centroid;
import com.vividsolutions.jts.geom.*;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

public abstract class BoundariesShapefile extends TomtomShapefile {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private final String osmLevel;
    private final String tomtomLevel;

    protected final OsmLevelGenerator osmLevelGenerator;
    protected final NameProvider nameProvider;
    protected final String zone;

    protected BoundariesShapefile(String filename, int tomtomLevel, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator) {
        super(filename);
        String[] split = filename.split("/");
        zone = split[split.length - 1].split("_[_2]")[0];

        this.osmLevelGenerator = osmLevelGenerator;
        this.osmLevel = osmLevelGenerator.getOsmLevel(zone, tomtomLevel);
        this.tomtomLevel = String.valueOf(tomtomLevel);
        this.nameProvider = nameProvider;
        if (new File(filename).exists()) {
            this.nameProvider.loadFromFile("___an.dbf");
        }
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        Long extId = feature.getLong("ID");
        Optional<String> order = ofNullable(feature.getString("ORDER0" + tomtomLevel));

        Map<String, String> tags = newHashMap();
        tags.putAll(nameProvider.getAlternateNames(extId));
        tags.put("ref:tomtom", String.valueOf(extId));
        order.ifPresent(alpha3 -> tags.put("ref:INSEE", getInseeWithAlpha3(alpha3)));

        List<RelationMember> members = newArrayList();
        String name = feature.getString("NAME");
        if (name != null) {
            Map<String, String> wayTags = putWayTags(name);
            Map<String, String> pointTags = newHashMap(tags);
            pointTags.put("name", name);
            MultiPolygon multiPolygon = feature.getMultiPolygon();
            addPointWithRoleLabel(serializer, members, pointTags, multiPolygon);
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
                    for (Geometry geom : LongLineSplitter.split(polygon.getInteriorRingN(j), 100)) {
                        addRelationMember(serializer, members, wayTags, (LineString) geom, "inner");
                    }
                }
                for (Geometry geom : LongLineSplitter.split(polygon.getExteriorRing(), 100)) {
                    addRelationMember(serializer, members, wayTags, (LineString) geom, "outer");
                }
            }

            putRelationTags(tags, wayTags);

            finishRelation(serializer, tags, members, feature);
        }
    }

    private Map<String, String> putWayTags(String name) {
        return of("name", name, "boundary", "administrative", "admin_level", osmLevel);
    }

    private void putRelationTags(Map<String, String> tags, Map<String, String> wayTags) {
        tags.putAll(wayTags);
        tags.put("type", "boundary");
        tags.put("layer", osmLevel);
    }

    private String getInseeWithAlpha3(String alpha3) {
        String alpha32 = alpha3;
        if (CountryCode.getByCode(alpha3) == null) {
            alpha32 = alpha3.substring(0, alpha3.length() - 1);
        }
        return CountryCode.getByCode(alpha32) == null ? alpha3 : valueOf(CountryCode.getByCode(alpha32).getNumeric());
    }

    protected void finishRelation(GeometrySerializer serializer, Map<String, String> tags, List<RelationMember> members, Feature feature) {
        serializer.writeRelation(members, tags);
    }

    private void addRelationMember(GeometrySerializer serializer, List<RelationMember> members, Map<String, String> wayTags, LineString geom, String memberRole) {
        Long wayId = serializer.writeBoundary(geom, wayTags);
        members.add(new RelationMember(wayId, Way, memberRole));
    }

    private void addPointWithRoleLabel(GeometrySerializer serializer, List<RelationMember> members, Map<String, String> tags, MultiPolygon multiPolygon) {
        Coordinate centPt = Centroid.getCentroid(multiPolygon);
        Optional<Node> node = serializer.writePoint(GEOMETRY_FACTORY.createPoint(centPt), tags);
        node.ifPresent(nodeLabel -> members.add(new RelationMember(nodeLabel.getId(), Node, "label")));
    }
}

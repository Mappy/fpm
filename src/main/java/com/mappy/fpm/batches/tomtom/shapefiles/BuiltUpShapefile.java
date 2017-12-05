package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.mappy.fpm.batches.tomtom.helpers.Centroid;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.LongLineSplitter;
import com.vividsolutions.jts.geom.*;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.vividsolutions.jts.algorithm.Centroid.getCentroid;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

public class BuiltUpShapefile extends TomtomShapefile {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private final NameProvider nameProvider;
    private final TownTagger townTagger;

    @Inject
    public BuiltUpShapefile(TomtomFolder folder, NameProvider nameProvider, TownTagger townTagger) {
        super(folder.getFile("bu.shp"));
        this.nameProvider = nameProvider;
        this.townTagger = townTagger;

        if (new File(folder.getFile("bu.shp")).exists()) {
            nameProvider.loadFromCityFile("smnm.dbf");
        }
    }

    @Override
    public String getOutputFileName() {
        return "bu";
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        String name = feature.getString("NAME");
        if (name != null) {
            Long extId = feature.getLong("ID");

            Map<String, String> tags = nameProvider.getAlternateNames(extId);
            tags.put("ref:tomtom", String.valueOf(extId));
            tags.put("name", name);
            Centroid cityCenter = townTagger.getHamlet(extId);

            List<RelationMember> members = newArrayList();
            MultiPolygon multiPolygon = feature.getMultiPolygon();

            getLabel(serializer, tags, multiPolygon).ifPresent(members::add);

            getAdminCenter(serializer, name, cityCenter).ifPresent(members::add);

            members.addAll(addPolygons(serializer, name, multiPolygon, cityCenter));

            tags.putAll(of("type", "multipolygon", "landuse", "residential", "layer", "10"));
            serializer.writeRelation(members, tags);
        }
    }

    private Optional<RelationMember> getLabel(GeometrySerializer serializer, Map<String, String> tags, MultiPolygon multiPolygon) {
        Optional<Node> labelNode = serializer.writePoint(GEOMETRY_FACTORY.createPoint(getCentroid(multiPolygon)), tags);
        return labelNode.map(node -> new RelationMember(node.getId(), Node, "label"));
    }

    private Optional<RelationMember> getAdminCenter(GeometrySerializer serializer, String name, Centroid cityCenter) {

        if (cityCenter != null) {
            Map<String, String> adminTags = nameProvider.getAlternateCityNames(cityCenter.getId());
            adminTags.put("name", name);
            cityCenter.getPlace().ifPresent(p -> adminTags.put("place", p));
            ofNullable(cityCenter.getPostcode()).ifPresent(code -> adminTags.put("addr:postcode", code));

            if(serializer.containPoint(cityCenter.getPoint())) {
                cityCenter.getPoint().getCoordinate().x += 0.00001;
                cityCenter.getPoint().getCoordinate().y += 0.00001;
            }

            Optional<Node> node = serializer.writePoint(cityCenter.getPoint(), adminTags);
            Long adminCenter = node.map(Entity::getId).orElse(0L);
            return of(new RelationMember(adminCenter, Node, "admin_center"));
        } else {
            return empty();
        }
    }

    private List<RelationMember> addPolygons(GeometrySerializer serializer, String name, MultiPolygon multiPolygon, Centroid cityCenter) {
        List<RelationMember> result = newArrayList();

        Map<String, String> wayTags = newHashMap(of("name", name));
        ofNullable(cityCenter).map(Centroid::getPlace).ifPresent(p -> wayTags.put("place", p.get()));

        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
            for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
                for (Geometry geom : LongLineSplitter.split(polygon.getInteriorRingN(j), 100)) {
                    Long wayId = serializer.writeBoundary((LineString) geom, wayTags);
                    result.add(new RelationMember(wayId, Way, "inner"));
                }
            }
            for (Geometry geom : LongLineSplitter.split(polygon.getExteriorRing(), 100)) {
                Long wayId = serializer.writeBoundary((LineString) geom, wayTags);
                result.add(new RelationMember(wayId, Way, "outer"));
            }
        }

        return result;
    }
}
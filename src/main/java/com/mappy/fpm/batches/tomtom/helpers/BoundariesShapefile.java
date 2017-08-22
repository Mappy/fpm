package com.mappy.fpm.batches.tomtom.helpers;

import com.google.common.collect.Lists;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.LongLineSplitter;
import com.vividsolutions.jts.algorithm.Centroid;
import com.vividsolutions.jts.geom.*;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

public class BoundariesShapefile extends TomtomShapefile {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private final String adminLevel;
    private final NameProvider nameProvider;


    protected BoundariesShapefile(String filename, int adminLevel, NameProvider nameProvider) {
        super(filename);
        this.adminLevel = String.valueOf(adminLevel);
        this.nameProvider = nameProvider;
        this.nameProvider.loadFromFile("an.dbf", "NAME", false);
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        serialize(serializer, feature, Lists.newArrayList());
    }

    public void serialize(GeometrySerializer serializer, Feature feature, List<RelationMember> members) {
        String name = feature.getString("NAME");
        Long extId = feature.getLong("ID");
        if (name != null) {
            Map<String, String> tags = nameProvider.getAlternateNames(extId);
            tags.putAll(of(
                    "boundary", "administrative",
                    "admin_level", adminLevel,
                    "name", name,
                    "ref:tomtom", extId.toString()));
            MultiPolygon multiPolygon = feature.getMultiPolygon();
            Coordinate centPt = Centroid.getCentroid(multiPolygon);
            Optional<Node> node = serializer.writePoint(GEOMETRY_FACTORY.createPoint(centPt), of());
            node.ifPresent(node1 ->
                    members.add(new RelationMember(node1.getId(), Node, "admin_center"))
            );
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                for (Geometry geom : LongLineSplitter.split(polygon.getExteriorRing(), 100)) {

                    Way way = serializer.write((LineString) geom, tags);
                    members.add(new RelationMember(way.getId(), Way, "outer"));
                }
            }

            serializer.writeRelation(members, of(
                    "type",
                    "boundary",
                    "boundary",
                    "administrative",
                    "admin_level",
                    adminLevel,
                    "name",
                    name));
        }
    }

}

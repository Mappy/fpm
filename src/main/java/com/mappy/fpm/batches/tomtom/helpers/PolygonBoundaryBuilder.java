package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.LongLineSplitter;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

public class PolygonBoundaryBuilder {

    public static void addPolygons(GeometrySerializer serializer, List<RelationMember> members, MultiPolygon multiPolygon, Map<String, String> wayTags) {
        IntStream.range(0, multiPolygon.getNumGeometries()).forEach(i -> {
            Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);

            IntStream.range(0, polygon.getNumInteriorRing()).forEach(j -> addPolygonRelations(serializer, members, wayTags, polygon.getInteriorRingN(j), "inner"));

            addPolygonRelations(serializer, members, wayTags, polygon.getExteriorRing(), "outer");
        });
    }

    private static void addPolygonRelations(GeometrySerializer serializer, List<RelationMember> members, Map<String, String> wayTags, LineString exteriorRing, String memberRole) {
        LongLineSplitter.split(exteriorRing, 100)
                .forEach(geom -> addRelationMember(serializer, wayTags, geom, memberRole).ifPresent(members::add));
    }

    private static Optional<RelationMember> addRelationMember(GeometrySerializer serializer, Map<String, String> wayTags, LineString geom, String memberRole) {
        Optional<Long> wayId = serializer.writeBoundary(geom, wayTags);
        return wayId.map(aLong -> new RelationMember(aLong, Way, memberRole));
    }

}

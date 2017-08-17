package com.mappy.fpm.batches.tomtom.helpers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.LongLineSplitter;
import com.mappy.fpm.batches.utils.WriteFirst;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import lombok.Getter;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;
@WriteFirst
public class BoundariesShapefile extends TomtomShapefile {

    private final String adminLevel;


    protected BoundariesShapefile(String filename, int adminLevel) {
        super(filename);
        this.adminLevel = String.valueOf(adminLevel);
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        String name = feature.getString("NAME");
        Long extId = feature.getLong("ID");
        if (name != null) {
            MultiPolygon multiPolygon = feature.getMultiPolygon();
            List<RelationMember> members = Lists.newArrayList();
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                for (Geometry geom : LongLineSplitter.split(polygon.getExteriorRing(), 100)) {
                    Way way = serializer.write((LineString) geom, ImmutableMap.of(
                            "boundary",
                            "administrative",
                            "admin_level",
                            adminLevel,
                            "ref:tomtom",
                            extId.toString()));
                    members.add(new RelationMember(way.getId(), Way, "outer"));
                }
            }
            serializer.writeRelation(members, ImmutableMap.of(
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

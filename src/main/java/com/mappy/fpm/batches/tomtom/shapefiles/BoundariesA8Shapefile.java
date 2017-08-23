package com.mappy.fpm.batches.tomtom.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.RelationProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.LongLineSplitter;
import com.mappy.fpm.batches.utils.Order;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import javax.inject.Inject;
import java.util.List;

@Order(1)
public class BoundariesA8Shapefile extends BoundariesShapefile {

    RelationProvider relationProvider;


    @Inject
    public BoundariesA8Shapefile(TomtomFolder folder, RelationProvider relationProvider, NameProvider nameProvider) {

        super(folder.getFile("a8.shp"), 8, nameProvider);
        this.relationProvider = relationProvider;
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        List<RelationMember> relationMembers = Lists.newArrayList();
        String name = feature.getString("NAME");
        Long extId = feature.getLong("ID");
        if (name != null) {
            MultiPolygon multiPolygon = feature.getMultiPolygon();
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                for (Geometry geom : LongLineSplitter.split(polygon.getExteriorRing(), 100)) {
                    Way way = serializer.write((LineString) geom, ImmutableMap.of(
                            "boundary",
                            "administrative",
                            "admin_level",
                            "8",
                            "name",
                            name,
                            "ref:tomtom",
                            extId.toString()));
                    relationMembers.add(new RelationMember(way.getId(), EntityType.Way, "outer"));
                }
            }
        }
        relationProvider.putRelation(feature, relationMembers, name);
    }
}
package com.mappy.fpm.batches.tomtom.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.RelationProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.Order;
import com.vividsolutions.jts.geom.MultiPolygon;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Order(1)
public class BoundariesA8Shapefile extends BoundariesShapefile {

    RelationProvider relationProvider;


    @Inject
    public BoundariesA8Shapefile(TomtomFolder folder, RelationProvider relationProvider, NameProvider nameProvider) {

        super(folder.getFile("a8.shp"), 8, 8, nameProvider);
        this.relationProvider = relationProvider;
    }

    @Override
    public void writeRelations(GeometrySerializer serializer, List<RelationMember> members, Map<String, String> tags) {
    }

    @Override
    public void addPoint(GeometrySerializer serializer, List<RelationMember> members, String name, MultiPolygon multiPolygon) {
    }

    @Override
    public void addRelations(GeometrySerializer serializer, Feature feature, List<RelationMember> members, String name, Map<String, String> tags, ImmutableMap<String, String> wayTags) {
        relationProvider.putRelation(feature, members, tags);
    }
}
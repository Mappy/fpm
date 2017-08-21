package com.mappy.fpm.batches.tomtom.shapefiles;

import com.google.common.collect.Lists;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.PopulationProvider;
import com.mappy.fpm.batches.tomtom.helpers.RelationProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;

import com.mappy.fpm.batches.utils.Order;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Inject;
import java.util.List;

@Order(1)
public class BoundariesA8Shapefile extends BoundariesShapefile {

    PopulationProvider populationProvider;
    RelationProvider relationProvider;


    @Inject
    public BoundariesA8Shapefile(TomtomFolder folder, PopulationProvider populationProvider, RelationProvider relationProvider) {

        super(folder.getFile("___a8.shp"), 8);
        this.populationProvider = populationProvider;
        this.relationProvider = relationProvider;
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        List<RelationMember> relationMembers = Lists.newArrayList();
        super.serialize(serializer, feature, relationMembers);
        populationProvider.putPopulation(feature);
        relationProvider.putRelation(feature, relationMembers);
    }
}

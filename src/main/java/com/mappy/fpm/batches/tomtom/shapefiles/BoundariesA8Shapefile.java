package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.PopulationProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;

import javax.inject.Inject;


public class BoundariesA8Shapefile extends BoundariesShapefile {

    PopulationProvider populationProvider;


    @Inject
    public BoundariesA8Shapefile(TomtomFolder folder, PopulationProvider populationProvider) {
        super(folder.getFile("a8.shp"), 8);
        this.populationProvider = populationProvider;
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        super.serialize(serializer, feature);
        populationProvider.putPopulation(feature);
    }
}

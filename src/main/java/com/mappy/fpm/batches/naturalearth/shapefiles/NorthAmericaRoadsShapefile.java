package com.mappy.fpm.batches.naturalearth.shapefiles;

import com.mappy.fpm.batches.naturalearth.RoadsShapefile;
import com.mappy.fpm.batches.utils.Feature;

import javax.inject.Inject;
import javax.inject.Named;

public class NorthAmericaRoadsShapefile extends RoadsShapefile {

    @Inject
    public NorthAmericaRoadsShapefile(@Named("com.mappy.fpm.naturalearth.data") String input) {
        super(input + "/ne_10m_roads_north_america.shp");
    }

    @Override
    public boolean accept(Feature feature) {
        return true;
    }
}
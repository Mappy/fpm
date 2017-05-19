package com.mappy.data.batches.naturalearth.shapefiles;

import com.mappy.data.batches.naturalearth.RoadsShapefile;
import com.mappy.data.batches.utils.Feature;

import javax.inject.Inject;
import javax.inject.Named;

public class NorthAmericaRoadsShapefile extends RoadsShapefile {

    @Inject
    public NorthAmericaRoadsShapefile(@Named("com.mappy.data.naturalearth.data") String input) {
        super(input + "/ne_10m_roads_north_america.shp");
    }

    @Override
    public boolean accept(Feature feature) {
        return true;
    }
}
package com.mappy.fpm.batches.naturalearth.shapefiles;

import com.mappy.fpm.batches.naturalearth.RoadsShapefile;
import com.mappy.fpm.batches.utils.Feature;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class WorldRoadsShapefile extends RoadsShapefile {

    private static final Set<String> rejectedCountries = newHashSet("USA", "CAN", "MEX");

    @Inject
    public WorldRoadsShapefile(@Named("com.mappy.fpm.naturalearth.data") String input) {
        super(input + "/ne_10m_roads.shp");
    }

    @Override
    public boolean accept(Feature feature) {
        return !rejectedCountries.contains(feature.getString("sov_a3"));
    }
}
package com.mappy.fpm.batches.naturalearth.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.naturalearth.NaturalEarthShapefile;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import javax.inject.Named;

public class LakesShapefile extends NaturalEarthShapefile {

    @Inject
    public LakesShapefile(@Named("com.mappy.data.naturalearth.data") String input) {
        super(input + "/ne_10m_lakes.shp");
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        serializer.write(feature.getMultiPolygon(), ImmutableMap.of( //
                "natural", "water", //
                "water", "lake"));
    }
}

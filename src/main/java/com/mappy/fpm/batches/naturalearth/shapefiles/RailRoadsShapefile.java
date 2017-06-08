package com.mappy.fpm.batches.naturalearth.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.naturalearth.NaturalEarthShapefile;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import javax.inject.Named;

public class RailRoadsShapefile extends NaturalEarthShapefile {
    @Inject
    public RailRoadsShapefile(@Named("com.mappy.fpm.naturalearth.data") String input) {
        super(input + "/ne_10m_railroads.shp");
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        serializer.write(feature.getMultiLineString(), ImmutableMap.of("railway", "rail"));
    }
}
package com.mappy.data.batches.naturalearth.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.mappy.data.batches.naturalearth.NaturalEarthShapefile;
import com.mappy.data.batches.utils.Feature;
import com.mappy.data.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import javax.inject.Named;

public class AirportsShapefile extends NaturalEarthShapefile {
    @Inject
    public AirportsShapefile(@Named("com.mappy.data.naturalearth.data") String input) {
        super(input + "/ne_10m_airports.shp");
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        serializer.write(feature.getPoint(), ImmutableMap.of( //
                "aeroway", "aerodrome", //
                "name", feature.getString("name")));
    }
}
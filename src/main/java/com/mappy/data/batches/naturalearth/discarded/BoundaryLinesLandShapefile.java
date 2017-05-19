package com.mappy.data.batches.naturalearth.discarded;

import com.google.common.collect.ImmutableMap;
import com.mappy.data.batches.naturalearth.NaturalEarthShapefile;
import com.mappy.data.batches.utils.Feature;
import com.mappy.data.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import javax.inject.Named;

public class BoundaryLinesLandShapefile extends NaturalEarthShapefile {
    @Inject
    public BoundaryLinesLandShapefile(@Named("com.mappy.data.naturalearth.data") String input) {
        super(input + "/ne_10m_admin_0_boundary_lines_land.shp");
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        serializer.write(feature.getMultiLineString(), ImmutableMap.of( //
                "boundary", "administrative", //
                "admin_level", "2"));
    }
}
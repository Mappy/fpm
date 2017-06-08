package com.mappy.fpm.batches.naturalearth.discarded;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.naturalearth.NaturalEarthShapefile;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import javax.inject.Named;

public class BoundaryLinesLandShapefile extends NaturalEarthShapefile {
    @Inject
    public BoundaryLinesLandShapefile(@Named("com.mappy.fpm.naturalearth.data") String input) {
        super(input + "/ne_10m_admin_0_boundary_lines_land.shp");
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        serializer.write(feature.getMultiLineString(), ImmutableMap.of( //
                "boundary", "administrative", //
                "admin_level", "2"));
    }
}
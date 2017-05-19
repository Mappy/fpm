package com.mappy.data.batches.naturalearth.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.mappy.data.batches.naturalearth.NaturalEarthShapefile;
import com.mappy.data.batches.utils.Feature;
import com.mappy.data.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import javax.inject.Named;

public class UrbanAreasShapefile extends NaturalEarthShapefile {
    @Inject
    public UrbanAreasShapefile(@Named("com.mappy.data.naturalearth.data") String input) {
        super(input + "/ne_10m_urban_areas.shp");
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        serializer.write(feature.getPolygon(), ImmutableMap.of("landuse", "residential"));
    }
}
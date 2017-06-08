package com.mappy.fpm.batches.naturalearth.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.naturalearth.NaturalEarthShapefile;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Slf4j
public class OceansLabelsShapefile extends NaturalEarthShapefile {
    private static final Map<String, String> clazzes = ImmutableMap.of("ocean", "ocean", "sea", "sea");

    @Inject
    public OceansLabelsShapefile(@Named("com.mappy.fpm.naturalearth.data") String input) {
        super(input + "/ne_10m_geography_marine_polys.shp");
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        String name = feature.getString("name");
        String clazz = feature.getString("featurecla");
        if(name != null) {
            serializer.write(feature.getMultiPolygon().getInteriorPoint(), ImmutableMap.of(
                    "place",
                    clazzes.getOrDefault(clazz, "locality"),
                    "name",
                    name));
        }
    }
}
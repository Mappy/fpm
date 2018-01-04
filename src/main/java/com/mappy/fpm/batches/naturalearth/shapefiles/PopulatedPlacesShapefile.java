package com.mappy.fpm.batches.naturalearth.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mappy.fpm.batches.geonames.AlternateName;
import com.mappy.fpm.batches.geonames.Geonames;
import com.mappy.fpm.batches.naturalearth.NaturalEarthShapefile;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

public class PopulatedPlacesShapefile extends NaturalEarthShapefile {

    private final Geonames geonames;

    @Inject
    public PopulatedPlacesShapefile(@Named("com.mappy.fpm.naturalearth.data") String input, Geonames geonames) {
        super(input + "/ne_10m_populated_places.shp");
        this.geonames = geonames;
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        serializer.write(feature.getPoint(), tags(feature));
    }

    private ImmutableMap<String, String> tags(Feature feature) {
        Builder<String, String> capital = capital(feature);
        Double geonameid = feature.getDouble("GEONAMEID");
        if (geonameid != null) {
            List<AlternateName> frenchNames = geonames.frenchNames(geonameid.intValue());
            if (!frenchNames.isEmpty()) {
                capital = capital.put("name:fr", frenchNames.get(0).getValue());
            }
        }
        return capital
                .put("name", feature.getString("NAME"))
                .put("population", feature.getInteger("POP_MAX").toString())
                .put("place", feature.getInteger("POP_MAX") > 100000 ? "city" : feature.getInteger("POP_MAX") > 10000 ? "town" : "village")
                .build();
    }

    private static Builder<String, String> capital(Feature feature) {
        Builder<String, String> builder = ImmutableMap.builder();
        if (feature.getString("FEATURECLA").startsWith("Admin-0 capital")) {
            return builder.put("capital", "yes");
        }
        return builder;
    }
}
package com.mappy.data.batches.naturalearth.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;
import com.mappy.data.batches.geonames.AlternateName;
import com.mappy.data.batches.geonames.Geonames;
import com.mappy.data.batches.naturalearth.NaturalEarthShapefile;
import com.mappy.data.batches.utils.Feature;
import com.mappy.data.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Set;

public class PopulatedPlacesShapefile extends NaturalEarthShapefile {
    private static final Set<String> allowedClasses = Sets.newHashSet(
            "Admin-1 region capital",
            "Admin-0 region capital",
            "Admin-0 capital",
            "Historic place",
            "Scientific station",
            "Admin-1 capital",
            "Populated place",
            "Admin-0 capital alt");
    private final Geonames geonames;

    @Inject
    public PopulatedPlacesShapefile(@Named("com.mappy.data.naturalearth.data") String input, Geonames geonames) {
        super(input + "/ne_10m_populated_places.shp");
        this.geonames = geonames;
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        serializer.write(feature.getPoint(), tags(feature));
    }

    private ImmutableMap<String, String> tags(Feature feature) {
        Builder<String, String> capital = capital(feature);
        List<AlternateName> frenchNames = geonames.frenchNames(feature.getDouble("GEONAMEID").intValue());
        if (!frenchNames.isEmpty()) {
            capital = capital.put("name:fr", frenchNames.get(0).getValue());
        }
        return capital
                .put("name", feature.getString("NAME"))
                .put("population", feature.getInteger("POP_MAX").toString())
                .put("place", feature.getInteger("POP_MAX") > 100000 ? "city" : feature.getInteger("POP_MAX") > 10000 ? "town" : "village")
                .build();
    }

    private static Builder<String, String> capital(Feature feature) {
        Builder<String, String> builder = ImmutableMap.builder();
        String clazz = feature.getString("FEATURECLA");
        if (!allowedClasses.contains(clazz)) {
            throw new IllegalStateException("Unexpected populated places class: " + clazz);
        }
        if (clazz.startsWith("Admin-0 capital")) {
            return builder.put("capital", "yes");
        }
        return builder;
    }
}
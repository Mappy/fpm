package com.mappy.data.batches.naturalearth;

import com.google.common.collect.*;
import com.mappy.data.batches.utils.*;

import java.util.Set;

public abstract class RoadsShapefile extends NaturalEarthShapefile {
    private static final Set<String> allowedTypes = Sets.newHashSet(
            "Ferry Route",
            "Ferry, seasonal",
            "Major Highway",
            "Secondary Highway",
            "Beltway",
            "Road",
            "Unknown",
            "Bypass",
            "Track");

    protected RoadsShapefile(String filename) {
        super(filename);
    }

    public abstract boolean accept(Feature feature);

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        if (accept(feature)) {
            String type = feature.getString("type");
            if (type != null) {
                if (allowedTypes.contains(type)) {
                    serializer.write(feature.getMultiLineString(), tags(type));
                }
            }
        }
    }

    private static ImmutableMap<String, String> tags(String type) {
        if ("Ferry Route".equals(type) || "Ferry, seasonal".equals(type)) {
            return ImmutableMap.of("route", "ferry");
        }
        String highway = "unclassified";
        switch (type) {
            case "Major Highway":
                highway = "motorway";
                break;
            case "Secondary Highway":
            case "Beltway":
                highway = "trunk";
                break;
            case "Road":
                highway = "primary";
                break;
            case "Unknown":
            case "Bypass":
            case "Track":
            default:
                break;
        }
        return ImmutableMap.of("highway", highway);
    }
}
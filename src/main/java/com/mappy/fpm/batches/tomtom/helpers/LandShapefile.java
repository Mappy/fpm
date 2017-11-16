package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;

import java.io.File;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public abstract class LandShapefile extends TomtomShapefile {

    private final NameProvider nameProvider;

    protected LandShapefile(NameProvider nameProvider, String filename) {
        super(filename);
        this.nameProvider = nameProvider;
        if (new File(filename).exists()) {
            this.nameProvider.loadFromFile("lxnm.dbf", "NAME", false);
        }
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        if (7110 != feature.getInteger("FEATTYP")) {
            serializer.write(feature.getMultiPolygon(), completeTags(feature));
        }
    }

    private Map<String, String> completeTags(Feature feature) {
        Map<String, String> tags = newHashMap();

        tags.put("ref:tomtom", String.valueOf(feature.getLong("ID")));

        String name = feature.getString("NAME");
        if (name != null) {
            tags.put("name", name);
        }
        tags.putAll(nameProvider.getAlternateNames(feature.getLong("ID")));

        switch (feature.getInteger("FEATTYP")) {
            case 3110: // Built-up area
                tags.put("landuse", "residential");
                break;
            case 7120: // Forest (Woodland)
                tags.put("landuse", "forest");
                break;
            case 7170: // Park
            case 9732: // Airport Ground
                tags.put("landuse", "grass");
                break;
            case 7180: // Island
                tags.put("place", "island");
                break;
            case 9715: // Industrial Area
                tags.put("landuse", "industrial");
                break;
            case 9776: // Airport Runway
                tags.put("aeroway", "runway");
                break;
            case 9788: // Cemetery
                tags.put("landuse", "cemetery");
                break;
            case 9790: // Shopping Ground
                tags.put("landuse", "retail");
                break;
            case 9789:// Military Ground
                tags.put("landuse", "military");
                break;
            case 9768: // Stadium
                tags.put("leisure", "stadium");
                break;
            case 9780: // Institution
                tags.put("landuse", "institutional");
                break;
            case 9771: // University
                tags.put("amenity", "university");
                break;
            case 9756: // Parking area
                tags.put("amenity", "parking");
                break;
            case 9744: // Golf course
                tags.put("leisure", "golf_course");
                break;
            case 9733: // Amusement Park Ground
                tags.put("tourism", "theme_park");
                break;
            case 9748: // Hospital Ground
                tags.put("amenity", "hospital");
                break;
            case 9720: // Industrial Harbour Area
                tags.put("landuse", "port");
                break;
            case 9353: // Company Ground
                tags.put("landuse", "commercial");
                break;
            case 9710: // Beach, Dune and Plain Sand
                tags.put("natural", "beach");
                break;
            case 9725: // Moors & Heathland
                tags.put("natural", "grassland");
                break;
        }
        return tags;
    }
}

package com.mappy.fpm.batches.tomtom.helpers;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;

import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.stream;

@Getter
public enum Land {

    BUILT_UP(3110, -1, of("landuse", "residential")),
    PARK_PROTECTED_AREA(7170, 2, of("boundary", "protected_area")),
    PARK_GRASS(7170, -1, of("landuse", "grass")),
    ISLAND(7180, -1, of("place", "island")),
    COMPANY_GROUND(9353, -1, of("landuse", "commercial")),
    BEACH_DUNE_AND_PLAIN_SAND(9710, -1, of("natural", "beach")),
    INDUSTRIAL_AREA(9715, -1, of("landuse", "industrial")),
    INDUSTRIAL_HARBOUR_AREA(9720, -1, of("landuse", "port")),
    MOORS_AND_HEATHLAND(9725, -1, of("natural", "grassland")),
    AIRPORT_GROUND(9732, -1, of("aeroway", "aerodrome")),
    AMUSEMENT_PARK_GROUND(9733, -1, of("tourism", "theme_park")),
    GOLF_COURSE(9744, -1, of("leisure", "golf_course")),
    HOSPITAL_GROUND(9748, -1, of("amenity", "hospital")),
    PARKING_AREA(9756, -1, of("amenity", "parking")),
    STADIUM(9768, -1, of("leisure", "stadium")),
    UNIVERSITY(9771, -1, of("amenity", "university")),
    AIRPORT_RUNWAY(9776, -1, of("aeroway", "runway")),
    INSTITUTION(9780, -1, of("landuse", "institutional")),
    CEMETERY(9788, -1, of("landuse", "cemetery")),
    MILITARY_GROUND(9789, -1, of("landuse", "military")),
    SHOPPING_GROUND(9790, -1, of("landuse", "retail"));

    private final Integer tomtomFeattype;
    private final Integer displayType;
    private final ImmutableMap<String, String> osmTag;

    Land(Integer tomtomFeattype, Integer displayType, ImmutableMap<String, String> osmTag) {
        this.tomtomFeattype = tomtomFeattype;
        this.displayType = displayType;
        this.osmTag = osmTag;
    }

    public static Optional<Land> getLand(Integer tomtomFeattype, Integer displayType) {
        return stream(Land.values()).filter(land -> tomtomFeattype.equals(land.tomtomFeattype) && displayType >= land.displayType).findFirst();
    }
}

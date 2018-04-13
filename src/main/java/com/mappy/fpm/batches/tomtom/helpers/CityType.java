package com.mappy.fpm.batches.tomtom.helpers;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum CityType {

    OTHER_SIMPLE(0, false, "isolated_dwelling"),
    OTHER_IMPORTANT(0, true, "village"),
    ADMINISTRATIVE_AREA_SIMPLE(1, false, "town"),
    ADMINISTRATIVE_AREA_IMPORTANT(1, true, "city"),
    ADMINISTRATIVE_PLACE_SIMPLE(2, false, "town"),
    ADMINISTRATIVE_PLACE_IMPORTANT(2, true, "city"),
    POSTAL(4, null, "locality"),
    BUILT_UP_AREA(8, null, "quarter"),
    CENSUS(16, null, "quarter"),
    HAMLET(32, null, "hamlet"),
    NEIGHBORHOOD(64, null, "neighbourhood");

    private final Integer key;
    private final Boolean important;
    private final String value;

    CityType(Integer key, Boolean important, String value) {
        this.key = key;
        this.important = important;
        this.value = value;
    }

    public static String getOsmValue(Integer citytyp, Integer adminclass) {
        return Stream.of(values())
                .filter(cityType -> isTheBiggestCityType(citytyp, isAnImportantCity(adminclass), cityType))
                .map(CityType::getValue)
                .findFirst().orElse("town");
    }

    private static boolean isAnImportantCity(Integer adminclass) {
        return adminclass < 8;
    }

    private static boolean isTheBiggestCityType(Integer citytyp, Boolean adminclass, CityType cityType) {
        return cityType.getKey().equals(citytyp) && (cityType.getImportant() == null || cityType.getImportant().equals(adminclass));
    }
}



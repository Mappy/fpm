package com.mappy.fpm.batches.tomtom.download;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TomtomFile {
    NETWORK("mn", "_nw."),
    MANEUVER("mn", "_mn."),
    MANEUVER_PRIORITY("mn", "_mp."),
    LAND_COVERS("mn", "_lc."),
    LAND_USES("mn", "_lu."),
    WATER_AREAS("mn", "_wa."),
    WATER_LINES("mn", "_wl."),
    ALTERNATE_CITY_NAMES("mn", "_smnm."),
    COASTLINES("mn", "_bl."),
    RAILROADS("mn", "_rr."),
    GEOCODE_INFORMATION("mn", "_gc."),
    SPEEDS_RESTRICTIONS("mn", "_sr."),
    SIGNPOST_INFORMATION("mn", "_si."),
    SIGNPOST_PATHS("mn", "_si."),
    SIGNPOST("mn", "_sg."),
    LAND_DIRECTIONS("mn", "_ld."),
    BOUNDARIES_LEVEL0_COUNTRY("mn", "_a0."),
    BOUNDARIES_LEVEL1("mn", "_a1."),
    BOUNDARIES_LEVEL2("mn", "_a2."),
    BOUNDARIES_LEVEL3("mn", "_a3."),
    BOUNDARIES_LEVEL4("mn", "_a4."),
    BOUNDARIES_LEVEL5("mn", "_a5."),
    BOUNDARIES_LEVEL6("mn", "_a6."),
    BOUNDARIES_LEVEL7("mn", "_a7."),
    BOUNDARIES_EXTENDED_CITY("mn", "_oa07."),
    BOUNDARIES_LEVEL8_CITY("mn", "_a8."),
    BOUNDARIES_LEVEL9("mn", "_a9."),
    ALTERNATE_NAMES("mn", "_an."),
    BUILT_UP_AREA("mn", "_bu."),
    TIME_DOMAIN("mn", "_td."),
    LAND_USE_AND_LAND_COVER_ALTERNATE_NAMES("mn", "_lxnm."),
    TRANSPORTATION_ELEMENT_BELONGING_TO_AREA("mn", "_ta."),

    NETWORK_PROFILE_LINK("sp", "_hsnp."),
    HISTORICAL_SPEED_PROFILES("sp", "_hspr."),

    HIGH_QUALITY_LANDUSE("2dcmnb", "_2dtb."),
    BUILDING("2dcmnb", "_2dtb."),

    OUTERWORLD("outerworld", "nw.");


    @Getter
    private final String product;
    @Getter
    private final String value;

    TomtomFile(String product, String value) {
        this.product = product;
        this.value = value;
    }

    public static List<String> allTomtomFiles(String product) {
        return Stream.of(TomtomFile.values())
                .filter(tomtomFile -> product.equals(tomtomFile.product))
                .map(TomtomFile::getValue)
                .collect(Collectors.toList());
    }
}
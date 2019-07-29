package com.mappy.fpm.batches.tomtom.download;

import lombok.Getter;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public enum TomtomFile {
    NETWORK("mn", "_nw."),
    ROAD_RESTRICTIONS("mn", "_rs."),
    MANEUVER("mn", "_mn."),
    MANEUVER_PRIORITY("mn", "_mp."),
    LAND_COVERS("mn", "_lc."),
    LAND_USES("mn", "_lu."),
    WATER_AREAS("mn", "_wa."),
    WATER_LINES("mn", "_wl."),
    CITIES("mn", "_sm."),
    ALTERNATE_CITY_NAMES("mn", "_smnm."),
    COASTLINES("mn", "_bl."),
    RAILROADS("mn", "_rr."),
    GEOCODE_INFORMATION("mn", "_gc."),
    SPEEDS_RESTRICTIONS("mn", "_sr."),
    SPEEDS_TIMEDOMAINS("mn", "_st."),
    SIGNPOST_INFORMATION("mn", "_si."),
    SIGNPOST_PATHS("mn", "_sp."),
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
    ROUTE_NUMBERS("mn", "_rn."),
    POINTS_OF_INTEREST("mn", "_pi."),
    POINTS_OF_INTEREST_EXTENDED_ATTRIBUTES("mn", "_piea."),

    ROUTE_INERSECTION_INDEX("mn", "_ig."),
    ROUTE_INERSECTION("mn", "_is."),

    NETWORK_PROFILE_LINK("sp", "_hsnp."),
    HISTORICAL_SPEED_PROFILES("sp", "_hspr."),

    // FIXME: Use and filter
    HIGH_QUALITY_LANDUSE("2dcmnb", "_2dtb."),
    BUILDING("2dcmnb", "_2dtb."),
    /*BUILDING_FOOTPRINT("2dcm", "_2dbf."),

    // FIXME: Use and filter
    POI("mnpoi", "_pi."),
    POI_ADDRESS("mnpoi", "_piad."),
    POI_ATTRIBUTE_SET("mnpoi", "_pias."),
    POI_ATTRIBUTE_VALUE("mnpoi", "_piav."),
    POI_CODE("mnpoi", "_picd."),
    POI_CONVERSION_RECORDS("mnpoi", "_picn."),
    POI_NAME("mnpoi", "_pinm."),
    POI_RELATIONSHIP("mnpoi", "_pipr."),
    POI_SERVICE_IN_AREA("mnpoi", "_pisa."),
    POI_XML_OBJECT_REFERENCE("mnpoi", "_pixo."),
    POI_CITY_CENTER("mnpoi", "_sm."),
    POI_SETTLEMENT_CENTER_EXTENDED_ATTRIBUTES("mnpoi", "_smea."),
    POI_CITY_CENTER_NAMES("mnpoi", "_smnm."),

    // FIXME: Use and filter
    ANCHOR_POINT("mnap", "_at."),
    ADDRESS("mnap", "_atad."),
    ADDRESS_COMPONENT("atac", "_atac."),
    NAME_COMPONENT("mnap", "_atnc."),
    FEATURE_ASSOCIATED_WITH_ANCHOR_POINT("mnap", "_atat."),
    COMPONENT_FORMAT("mnap", "_atcf."),
    ANCHOR_POINT_ALONG_TRANSPORTATION_ELEMENT("mnap", "_atte."),*/


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
                .collect(toList());
    }
}

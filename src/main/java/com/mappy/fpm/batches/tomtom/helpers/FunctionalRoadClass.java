package com.mappy.fpm.batches.tomtom.helpers;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.stream;

@Getter
public enum FunctionalRoadClass {

    NOT_APPLICABLE(-1, false, false, of("highway", "unclassified")),

    MAJOR_ROAD(0, false, null, of("highway", "motorway", "foot", "no", "bicycle", "no")),
    MAJOR_ROAD_LINK(0, true, null, of("highway", "motorway_link", "foot", "no", "bicycle", "no")),

    LESS_THAN_MOTORWAY(1, false, false, of("highway", "primary")),
    LESS_THAN_MOTORWAY_LINK(1, true, false, of("highway", "primary_link")),

    LESS_THAN_MOTORWAY_FREEWAY(1, false, true, of("highway", "trunk", "foot", "no", "bicycle", "no")),
    LESS_THAN_MOTORWAY_FREEWAY_LINK(1, true, true, of("highway", "trunk_link", "foot", "no", "bicycle", "no")),

    OTHER_MAJOR_ROAD(2, false, null, of("highway", "primary")),
    OTHER_MAJOR_ROAD_link(2, true, null, of("highway", "primary_link")),

    SECONDARY_ROAD(3, false, null, of("highway", "secondary")),
    SECONDARY_ROAD_LINK(3, true, null, of("highway", "secondary_link")),

    LOCAL_CONNECTING_ROAD(4, false, null, of("highway", "secondary")),
    LOCAL_CONNECTING_ROAD_LINK(4, true, null, of("highway", "secondary_link")),

    LOCAL_ROAD_OF_HIGH_IMPORTANCE(5, false, null, of("highway", "secondary")),
    LOCAL_ROAD_OF_HIGH_IMPORTANCE_LINK(5, true, null, of("highway", "secondary_link")),

    LOCAL_ROAD(6, false, null, of("highway", "residential")),
    LOCAL_ROAD_LINK(6, true, null, of("highway", "tertiary_link")),

    LOCAL_ROAD_OF_MINOR_IMPORTANCE(7, false, null, of("highway", "residential")),
    LOCAL_ROAD_OF_MINOR_IMPORTANCE_LINK(7, true, null, of("highway", "tertiary_link")),

    OTHER_ROAD(8, false, null, of("highway", "unclassified"));

    private final int value;
    private final Boolean isSlip;
    private final Boolean isFreeway;
    private final Map<String, String> tags;

    FunctionalRoadClass(int value, Boolean isSlip, Boolean isFreeway, Map<String, String> tags) {
        this.value = value;
        this.isSlip = isSlip;
        this.isFreeway = isFreeway;
        this.tags = tags;
    }

    public static Optional<FunctionalRoadClass> getFonctionalRoadClass(int value, Boolean isSlip, Boolean isFreeway){
        return stream(FunctionalRoadClass.values())
                .filter(functionalRoadClass -> functionalRoadClass.value == value && functionalRoadClass.isSlip == isSlip && (functionalRoadClass.isFreeway == null || functionalRoadClass.isFreeway == isFreeway))
                .findFirst();
    }
}

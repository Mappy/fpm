package com.mappy.fpm.batches.tomtom.helpers;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.of;

@Getter
public enum FormOfWay {
    PART_OF_MOTORWAY(1, of("foot", "no", "bicycle", "no")),
    PART_OF_MULTI_CARRIAGEWAY(2, of()),
    PART_OF_SINGLE_CARRIAGEWAY(3, of()),
    ROUNDABOUT(4, of("junction", "roundabout")),
    PARKING_PLACE(6, of("highway", "service", "service", "parking_aisle")),
    PARKING_GARAGE_BUILDING(7, of()),
    UNSTRUCTURED_TRAFFIC_SQUARE(8, of()),
    SLIP_ROAD(10, of()),
    SERVICE_ROAD(11, of("highway", "service")),
    ENTRANCE_EXIT_CAR_PARK(12, of("highway", "service", "service", "parking_aisle")),
    PEDESTRIAN(14, of("highway", "pedestrian")),
    WALKWAY(15, of("highway", "footway")),
    SPECIAL_TRAFFIC_FIGURE(17, of()),
    GALLERY(18, of("highway", "footway", "tunnel", "yes")),
    STAIRS(19, of("highway", "steps")),
    ROAD_OF_AUTHORITIES(19, of()),
    CONNECTOR(20, of()),
    CUL_DE_SAC(22, of());

    private final int value;
    private final Map<String, String> tags;

    FormOfWay(int value, Map<String, String> tags) {
        this.value = value;
        this.tags = tags;
    }

    public static Optional<FormOfWay> getFormOfWay(Integer value) {
        return Stream.of(values()).filter(f -> f.value == value).findFirst();
    }

    public boolean is(Integer input) {
        return input != null && value == input;
    }
}

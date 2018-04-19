package com.mappy.fpm.batches.tomtom.dbf.transportationarea;

import java.util.stream.Stream;

public enum TransportationElementType {
    ROAD_ELEMENT(4110, true),
    FERRY_CONNECTION_ELEMENT(4130, true),
    ADDRESS_AREA_BOUNDARY_ELEMENT(4165, false);

    public final Integer code;
    public final Boolean withArea;

    public static Boolean withArea(Integer code) {
        return Stream.of(values())
                .filter(transportationElementType -> transportationElementType.withArea)
                .anyMatch(transportationElementType -> code.equals(transportationElementType.code));
    }

    TransportationElementType(Integer code, Boolean withArea) {
        this.code = code;
        this.withArea = withArea;
    }
}

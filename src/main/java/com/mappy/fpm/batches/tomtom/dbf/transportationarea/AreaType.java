package com.mappy.fpm.batches.tomtom.dbf.transportationarea;

import java.util.stream.Stream;

public enum AreaType {
    ADMINISTRATIVE_AREA_ORDER_8(1119, false),
    ADMINISTRATIVE_AREA_ORDER_9(1120, false),
    BUILT_UP_AREA(3110, true);

    public final Integer code;
    public final Boolean isABuiltUp;

    public static Boolean isTheMinimumAreaType(Integer code, Boolean needBuiltUp) {
        return Stream.of(values())
                .filter(areaType1 -> needBuiltUp.equals(areaType1.isABuiltUp))
                .anyMatch(areaType1 -> code.equals(areaType1.code));
    }

    AreaType(Integer code, Boolean isABuiltUp) {
        this.code = code;
        this.isABuiltUp = isABuiltUp;
    }
}

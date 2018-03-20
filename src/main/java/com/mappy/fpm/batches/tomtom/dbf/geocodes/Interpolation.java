package com.mappy.fpm.batches.tomtom.dbf.geocodes;

import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

@Getter
public enum Interpolation {
    EVEN(2, "even"),
    ODD(3, "odd"),
    MIXED(4, "all"),
    IRREGULAR_HOUSE_NUMBER(5, "irregular:tomtom"),
    ALPHA_NUMERIC_MIXED(6, "alphabetic");

    private final Integer tomtomStructure;
    private final String osmValue;

    Interpolation(Integer tomtomStructure, String osmValue){
        this.tomtomStructure = tomtomStructure;
        this.osmValue = osmValue;
    }

    public static Optional<String> getOsmValue(Integer tomtomStructure){
        return Stream.of(values())
                .filter(interpolation -> tomtomStructure.equals(interpolation.tomtomStructure))
                .map(interpolation -> interpolation.osmValue)
                .findFirst();
    }


}

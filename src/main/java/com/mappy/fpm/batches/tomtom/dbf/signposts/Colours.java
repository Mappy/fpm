package com.mappy.fpm.batches.tomtom.dbf.signposts;

import java.util.stream.Stream;

public enum Colours {
    RED("2500", "red"), MORE_RED("205", "red"), GREEN("5", "green"), YELLOW("902", "yellow"), WHITE(null, "white");


    public final String code;
    public final String value;


    Colours(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public static String getColorOrWhite(String code) {
        return Stream.of(Colours.values())
                .filter(colours -> code.equals(colours.code))
                .findFirst()
                .orElse(WHITE).value;
    }


}

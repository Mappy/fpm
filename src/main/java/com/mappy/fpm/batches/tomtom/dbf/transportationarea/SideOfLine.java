package com.mappy.fpm.batches.tomtom.dbf.transportationarea;

public enum SideOfLine {
    BOTH_SIDES(0),
    LEFT(1),
    RIGHT(2);

    public final Integer value;

    SideOfLine(Integer value) {
        this.value = value;
    }
}

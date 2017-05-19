package com.mappy.data.batches.tomtom.dbf.lanes;

public enum Direction {
    Straight(0, "through"), Slight_Right(1, "slight_right"), Right(2, "right"), Sharp_Right(3, "sharp_right"), Uturn_Left(4, "reverse"), //
    Sharp_Left(5, "sharp_left"), Left(6, "left"), Slight_Left(7, "slight_left"), UTurn_Right(8, "reverse");

    public final int mask;
    public final String text;

    Direction(int mask, String text) {
        this.mask = mask;
        this.text = text;
    }
}
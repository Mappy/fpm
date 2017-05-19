package com.mappy.data.batches.tomtom.helpers;

import lombok.Getter;

import static java.util.Arrays.stream;

public enum Frc {

    NOT_APPLICABLE(-1), MAJOR_ROAD(0), LESS_THAN_MOTORWAY(1), OTHER_MAJOR_ROAD(2), SECONDARY_ROAD(3), LOCAL_CONNECTING_ROAD(4), LOCAL_ROAD_OF_HIGH_IMPORTANCE(5), LOCAL_ROAD(6), LOCAL_ROAD_OF_MINOR_IMPORTANCE(7), OTHER_ROAD(8);

    @Getter
    private final int value;

    Frc(int value) {
        this.value = value;
    }

    public static Frc valueOf(int value){
        return stream(Frc.values()).filter(frc -> frc.value == value).findFirst().get();
    }

    public boolean is(Integer input) {
        return input != null && value == input;
    }

    public boolean is(String input) {
        return String.valueOf(value).equals(input);
    }
}

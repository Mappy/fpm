package com.mappy.data.batches.tomtom.helpers;

import lombok.Getter;

public enum Fow {
    PART_OF_MOTORWAY(1), PARKING_PLACE(6), PARKING_GARAGE_BUILDING(7), SLIP_ROAD(10), ENTRANCE_EXIT_CAR_PARK(12), ROUNDABOUT(4), PEDESTRIAN(14), WALKWAY(15), STAIRS(19);

    @Getter
    private final int value;

    Fow(int value) {
        this.value = value;
    }

    public boolean is(Integer input) {
        return input != null && value == input;
    }

    public boolean is(String input) {
        return String.valueOf(value).equals(input);
    }
}

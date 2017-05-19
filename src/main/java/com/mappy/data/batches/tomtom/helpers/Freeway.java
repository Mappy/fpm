package com.mappy.data.batches.tomtom.helpers;

import lombok.Getter;

public enum Freeway {
    NO_PART_OF_FREEWAY(0), PART_OF_FREEWAY(1);

    @Getter
    private final int value;

    Freeway(int value) {
        this.value = value;
    }

    public boolean is(Integer input) {
        return input != null && value == input;
    }

    public boolean is(String input) {
        return String.valueOf(value).equals(input);
    }
}

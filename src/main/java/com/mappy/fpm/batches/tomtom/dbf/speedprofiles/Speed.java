package com.mappy.fpm.batches.tomtom.dbf.speedprofiles;

import lombok.Data;

import java.util.Optional;

@Data
public class Speed {
    private final Long id;
    private final Integer direction;
    private final Optional<Integer> freeFlow;
    private final Optional<Integer> weekDay;
    private final Optional<Integer> weekend;
    private final Optional<Integer> week;
    private final int[] profiles;

    public enum SpeedDirection {
        positive, negative
    }

    @Data
    public static class PrecomputeSpeedProfile {
        private final String profile;
        private final double min;
    }

    public SpeedDirection direction() {
        return direction == 2 ? SpeedDirection.positive : SpeedDirection.negative;
    }
}
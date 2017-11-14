package com.mappy.fpm.batches.tomtom.dbf.speedprofiles;

import lombok.Data;

import java.util.List;

@Data
public class Speed {

    private final Long id;
    private final Integer direction;
    private final Integer freeFlow;
    private final Integer weekDay;
    private final Integer weekend;
    private final Integer week;
    private final List<Integer> profiles;

    @Data
    public static class PrecomputeSpeedProfile {

        private final String profile;
        private final double min;
    }

    public String direction() {
        return direction == 2 ? "positive" : "negative";
    }
}
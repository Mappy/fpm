package com.mappy.fpm.batches.tomtom.dbf.maneuvers;

import lombok.Data;

@Data
public class Maneuver {

    private final Long junctionId;
    private final Long id;
    private final Integer featType;

    public boolean isNotPrivateRoad() {
        // todo check if we need also private road 2102
        return featType == 2101 || featType == 2103;
    }

    public static Maneuver fromFeature(Object[] entry) {
        return new Maneuver(
                ((Double) entry[4]).longValue(),
                ((Double) entry[0]).longValue(),
                ((Double) entry[1]).intValue());
    }
}
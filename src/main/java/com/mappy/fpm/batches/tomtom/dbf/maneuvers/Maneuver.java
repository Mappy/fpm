package com.mappy.fpm.batches.tomtom.dbf.maneuvers;

import lombok.Data;

import com.mappy.fpm.batches.utils.Feature;

@Data
public class Maneuver {
    private final Long junctionId;
    private final Long id;
    private final Integer featType;

    public boolean isNotPrivateRoad() {
        // todo check if we need also private road 2102
        return featType == 2101 || featType == 2103;
    }

    public static Maneuver fromFeature(Feature feature) {
        return new Maneuver(
                feature.getLong("JNCTID"),
                feature.getLong("ID"),
                feature.getInteger("FEATTYP"));
    }
}
package com.mappy.fpm.batches.tomtom.dbf.speedprofiles;

import lombok.Data;

import static com.google.common.collect.ComparisonChain.start;

@Data
public class SpeedProfile implements Comparable<SpeedProfile> {

    private final Integer id;
    private final Integer timeSlot;
    private final Double relSpeed;


    @Override
    public int compareTo(SpeedProfile speedProfile) {
        return start().compare(timeSlot, speedProfile.getTimeSlot()).compare(relSpeed, speedProfile.getRelSpeed()).compare(id, speedProfile.getId()).result();
    }
}

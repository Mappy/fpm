package com.mappy.fpm.batches.tomtom.dbf.speedprofiles;

import lombok.Data;

import com.google.common.collect.ComparisonChain;

@Data
public class SpeedProfile implements Comparable<SpeedProfile> {
    private final Integer id;
    private final TimeSlotSpeed timeSlotSpeed;

    @Override
    public int compareTo(SpeedProfile o) {
        return ComparisonChain.start().compare(timeSlotSpeed, o.getTimeSlotSpeed()).compare(id, o.getId()).result();
    }

    @Data
    public static class TimeSlotSpeed implements Comparable<TimeSlotSpeed> {
        private final Integer timeSlot;
        private final Double relSpeed;

        @Override
        public int compareTo(TimeSlotSpeed o) {
            return ComparisonChain.start().compare(timeSlot, o.getTimeSlot()).compare(relSpeed, o.getRelSpeed()).result();
        }
    }
}

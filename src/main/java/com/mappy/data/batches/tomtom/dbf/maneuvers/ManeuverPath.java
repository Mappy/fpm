package com.mappy.data.batches.tomtom.dbf.maneuvers;

import lombok.Data;

@Data
public class ManeuverPath {
    private final Long id;
    private final Long seqnr;
    private final Long trpelId;
    private final Long trpelType;

    public boolean isStreet() {
        // todo check if we need also ferry road 4130
        return trpelType == 4110;
    }

    public static ManeuverPath fromDbf(Object[] entry) {
        return new ManeuverPath(
                ((Double) entry[0]).longValue(),
                ((Double) entry[1]).longValue(),
                ((Double) entry[2]).longValue(),
                ((Double) entry[3]).longValue());
    }
}
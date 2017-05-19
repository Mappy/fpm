package com.mappy.data.batches.tomtom.dbf.speedrestrictions;

import lombok.Data;

@Data
public class SpeedRestriction {
    private final long id;
    private final int speed;
    private final Validity validity;

    public enum Validity {
        both, positive, negative
    }
}
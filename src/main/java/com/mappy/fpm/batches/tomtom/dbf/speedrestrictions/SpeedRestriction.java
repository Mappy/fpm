package com.mappy.fpm.batches.tomtom.dbf.speedrestrictions;

import lombok.Data;
import com.mappy.fpm.batches.tomtom.helpers.VehicleType;

@Data
public class SpeedRestriction {
    private final long id;
    private final int sequenceNumber;
    private final int speed;
    private final Validity validity;
    private final VehicleType vehicleType;

    public enum Validity {
        both, positive, negative
    }

}

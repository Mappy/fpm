package com.mappy.fpm.batches.tomtom.dbf.speedrestrictions;

import lombok.Data;

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

    public enum VehicleType {
        all, passengerCars, residentialVehicles, taxi, publicBus;

        public static VehicleType fromId(int id) {
            switch(id) {
                case 0:
                    return all;
                case 11:
                    return passengerCars;
                case 12:
                    return residentialVehicles;
                case 16:
                    return taxi;
                case 17:
                    return publicBus;
                default:
                    throw new RuntimeException("Unknown vehicle type: " + id);
            }
        }
    }
}

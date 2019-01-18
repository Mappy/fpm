package com.mappy.fpm.batches.tomtom.helpers;

public enum VehicleType {
    notApplicable, all, passengerCars, residentialVehicles, taxi, publicBus;

    public static VehicleType fromId(int id) {
        switch(id) {
            case -1:
                return notApplicable;
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

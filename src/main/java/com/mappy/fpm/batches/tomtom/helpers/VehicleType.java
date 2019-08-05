package com.mappy.fpm.batches.tomtom.helpers;

import java.util.EnumSet;

public enum VehicleType {
    notApplicable(EnumSet.noneOf(Vehicle.class)),
    all(EnumSet.allOf(Vehicle.class)),
    passengerCars(EnumSet.of(Vehicle.passengerCar)),
    residentialVehicles(EnumSet.of(Vehicle.residentialVehicle)),
    taxi(EnumSet.of(Vehicle.taxi)),
    publicBus(EnumSet.of(Vehicle.publicBus)),
    bicycle(EnumSet.of(Vehicle.bicycle));

    public enum Vehicle {
        passengerCar("motor_vehicle"),
        residentialVehicle("motor_vehicle"),
        taxi("taxi"),
        publicBus("bus"),
        bicycle("bicycle");

        public final String osmLabel;

        private Vehicle(String label) {
            osmLabel = label;
        }
    }

    public final EnumSet<Vehicle> vehicles;

    private VehicleType(EnumSet<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

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
            case 24:
                return bicycle;
            default:
                throw new RuntimeException("Unknown vehicle type: " + id);
        }
    }
}

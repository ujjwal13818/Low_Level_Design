package com.lld.problems.parkinglot;

public class Vehicle {
    private final VehicleType vehicleType;
    private final String vehicleId;

    public Vehicle(VehicleType vehicleType, String vehicleId) {
        this.vehicleType = vehicleType;
        this.vehicleId = vehicleId;
    }

    public VehicleType getVehicleType() { return vehicleType; }
    public String getVehicleId() { return vehicleId; }
}

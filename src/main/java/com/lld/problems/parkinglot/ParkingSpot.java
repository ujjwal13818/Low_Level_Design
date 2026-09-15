package com.lld.problems.parkinglot;

public class ParkingSpot {
    private final String spotId;
    private final SpotType spotType;
    private boolean isOccupied;
    public ParkingSpot(String spotId, SpotType spotType) {
        this.spotId = spotId;
        this.spotType = spotType;
        this.isOccupied = false;
    }

    public String getSpotId() {
        return spotId;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void occupy() { isOccupied = true; }
    public void vacate() { isOccupied = false; }

    public boolean canFitVehicle(Vehicle vehicle) {
        if(isOccupied) return false;
        return switch (spotType) {
            case MOTORCYCLE -> vehicle.getVehicleType() == VehicleType.MOTORCYCLE;
            case COMPACT -> vehicle.getVehicleType() == VehicleType.COMPACT || vehicle.getVehicleType() == VehicleType.MOTORCYCLE;
            case LARGE -> true;
        };
    }
}

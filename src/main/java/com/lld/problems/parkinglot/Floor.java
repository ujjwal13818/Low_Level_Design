package com.lld.problems.parkinglot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Floor {
    private final int floorNo;
    private Map<SpotType, List<ParkingSpot>> spotsByType;

    public Floor(int floorNo, List<ParkingSpot> spots) {
        this.floorNo = floorNo;
        this.spotsByType = new HashMap<>();
        for(ParkingSpot spot: spots) {
            SpotType type =  spot.getSpotType();
            if(spotsByType.containsKey(type)) {
                spotsByType.get(type).add(spot);
            }
            else {
                spotsByType.put(type, new ArrayList<>(List.of(spot)));
            }
        }
    }

    public int getFloorNo() {
        return floorNo;
    }

    public ParkingSpot findAvailableSpot(Vehicle vehicle) {
        List<SpotType> priorityOrder = getPriorityOrder(vehicle.getVehicleType());

        for (SpotType type : priorityOrder) {
            List<ParkingSpot> candidates = spotsByType.getOrDefault(type, List.of());
            for (ParkingSpot spot : candidates) {
                if (!spot.isOccupied()) {
                    return spot;
                }
            }
        }
        return null;
    }

    private List<SpotType> getPriorityOrder(VehicleType vehicleType) {
        return switch (vehicleType) {
            case MOTORCYCLE -> List.of(SpotType.MOTORCYCLE, SpotType.COMPACT, SpotType.LARGE);
            case COMPACT -> List.of(SpotType.COMPACT, SpotType.LARGE);
            case LARGE -> List.of(SpotType.LARGE);
        };
    }
}

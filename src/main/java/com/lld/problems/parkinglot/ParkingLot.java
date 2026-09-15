package com.lld.problems.parkinglot;

import java.util.List;

public class ParkingLot {
    private final List<Floor> floors;
    private final FeeStrategy feeStrategy;
    private int ticketCounter;

    public ParkingLot(List<Floor> floors, FeeStrategy feeStrategy) {
        this.floors = floors;
        this.feeStrategy = feeStrategy;
        this.ticketCounter = 0;
    }

    public ParkingTicket parkVehicle(Vehicle vehicle) {
        for (Floor floor : floors) {
            ParkingSpot spot = floor.findAvailableSpot(vehicle);
            if (spot != null) {
                spot.occupy();
                ticketCounter++;
                String ticketId = "T" + ticketCounter;
                return new ParkingTicket(ticketId, vehicle, spot, System.currentTimeMillis());
            }
        }
        throw new IllegalStateException("No available spot for this vehicle: " + vehicle.getVehicleId());
    }

    public double releaseVehicle(ParkingTicket ticket) {
        ticket.markExit(System.currentTimeMillis());
        ticket.getSpot().vacate();
        return feeStrategy.calculateFee(ticket);
    }
}
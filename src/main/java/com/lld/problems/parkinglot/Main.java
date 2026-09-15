package com.lld.problems.parkinglot;

import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        List<ParkingSpot> floor1Spots = List.of(
                new ParkingSpot("F1-M1", SpotType.MOTORCYCLE),
                new ParkingSpot("F1-C1", SpotType.COMPACT),
                new ParkingSpot("F1-L1", SpotType.LARGE)
        );
        Floor floor1 = new Floor(1, floor1Spots);

        ParkingLot parkingLot = new ParkingLot(List.of(floor1), new HourlyFeeStrategy());

        Vehicle car = new Vehicle(VehicleType.COMPACT, "KA-01-1234");
        ParkingTicket ticket = parkingLot.parkVehicle(car);
        System.out.println("Parked at: " + ticket.getSpot().getSpotId());

        Thread.sleep(2000); // simulate time passing

        double fee = parkingLot.releaseVehicle(ticket);
        System.out.println("Fee charged: " + fee);
    }
}
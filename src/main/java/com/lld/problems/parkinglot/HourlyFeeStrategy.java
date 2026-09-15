package com.lld.problems.parkinglot;

public class HourlyFeeStrategy implements FeeStrategy {
    @Override
    public double calculateFee(ParkingTicket parkingTicket) {
        double duration = parkingTicket.getExitTime() - parkingTicket.getEntryTime();
        double multiplier = 1000 * 60 * 60;
        double durationInHours = Math.ceil(duration / multiplier);  // raw fraction, THEN ceil — nothing else
        return 20 * durationInHours;
    }
}

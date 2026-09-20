package com.lld.problems.splitwise;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExactSplitStrategy implements SplitStrategy {
    private static final double EPSILON = 0.01;

    @Override
    public Map<User, Double> calculateSplit(double totalAmount, List<User> participants, Optional<Map<User, Double>> inputValues) {
        Map<User, Double> splitDetails = inputValues.orElseThrow(() ->
                new IllegalArgumentException("Exact split requires input values"));

        double sum = 0.0;
        for (double amount : splitDetails.values()) {
            sum += amount;
        }

        if (Math.abs(sum - totalAmount) > EPSILON) {
            throw new IllegalArgumentException("Split amounts (" + sum + ") do not sum to total amount (" + totalAmount + ")");
        }

        return splitDetails;
    }
}
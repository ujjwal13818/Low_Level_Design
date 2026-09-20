package com.lld.problems.splitwise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PercentageSplitStrategy implements SplitStrategy {
    private static final double EPSILON = 0.01;

    @Override
    public Map<User, Double> calculateSplit(double totalAmount, List<User> participants, Optional<Map<User, Double>> inputValues) {
        Map<User, Double> percentages = inputValues.orElseThrow(() ->
                new IllegalArgumentException("Percentage split requires input values"));

        double percentSum = 0.0;
        for (double percent : percentages.values()) {
            percentSum += percent;
        }

        if (Math.abs(percentSum - 100.0) > EPSILON) {
            throw new IllegalArgumentException("Percentages must sum to 100, got: " + percentSum);
        }

        Map<User, Double> result = new HashMap<>();
        for (Map.Entry<User, Double> entry : percentages.entrySet()) {
            double share = totalAmount * entry.getValue() / 100.0;
            result.put(entry.getKey(), share);
        }
        return result;
    }
}

package com.lld.problems.splitwise;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SplitStrategy {
    public Map<User, Double> calculateSplit(double totalAmount, List<User> participants, Optional<Map<User, Double>> inputValues);
}

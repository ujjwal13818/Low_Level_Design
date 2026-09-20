package com.lld.problems.splitwise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EqualSplitStrategy implements SplitStrategy {
    @Override
    public Map<User, Double> calculateSplit(double totalAmount, List<User> participants, Optional<Map<User, Double>> inputValues) {

        int n = participants.size();
        double eachParticipantDue = totalAmount / n;
        double lastPersonShare = totalAmount - (eachParticipantDue * (n-1));

        Map<User, Double> result = new HashMap<>();

        for(int i = 0 ; i < n-1 ; i++) {
            result.put(participants.get(i), eachParticipantDue);
        }

        result.put(participants.get(n-1) , lastPersonShare);

        return result;

    }
}

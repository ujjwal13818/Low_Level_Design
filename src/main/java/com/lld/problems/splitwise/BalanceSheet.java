package com.lld.problems.splitwise;

import java.util.HashMap;
import java.util.Map;

public class BalanceSheet {
    private static final double EPSILON = 0.01;
    private final Map<User, Map<User, Double>> balances = new HashMap<>();

    public void addExpense(Expense expense) {
        User paidBy = expense.getPaidBy();
        Map<User, Double> split = expense.getSplitDetails();

        for (Map.Entry<User, Double> entry : split.entrySet()) {
            User participant = entry.getKey();
            double share = entry.getValue();

            if (participant.equals(paidBy)) continue; // payer doesn't owe themselves

            balances.computeIfAbsent(paidBy, k -> new HashMap<>())
                    .merge(participant, share, Double::sum);
        }
    }

    public void removeExpense(Expense expense) {
        User paidBy = expense.getPaidBy();
        Map<User, Double> split = expense.getSplitDetails();

        for (Map.Entry<User, Double> entry : split.entrySet()) {
            User participant = entry.getKey();
            double share = entry.getValue();

            if (participant.equals(paidBy)) continue;

            balances.get(paidBy).merge(participant, -share, Double::sum);
            if (Math.abs(balances.get(paidBy).get(participant)) < EPSILON) {
                balances.get(paidBy).remove(participant);
            }
        }
    }

    // returns how much `debtor` owes `creditor`
    public double getBalance(User debtor, User creditor) {
        double owesToCreditor = balances.getOrDefault(creditor, Map.of()).getOrDefault(debtor, 0.0);
        double owesFromCreditor = balances.getOrDefault(debtor, Map.of()).getOrDefault(creditor, 0.0);
        return owesToCreditor - owesFromCreditor;
    }
}
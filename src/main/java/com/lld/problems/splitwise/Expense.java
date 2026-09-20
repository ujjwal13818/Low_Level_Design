package com.lld.problems.splitwise;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class Expense {
    private final String expenseId;
    private final String description;
    private final double totalAmount;
    private final User paidBy;
    private final List<User> participants;
    private final Map<User, Double> splitDetails;
    private final LocalDate date;

    public Expense(String expenseId, String description, double totalAmount, User paidBy,
                   List<User> participants, Map<User, Double> splitDetails, LocalDate date) {
        this.expenseId = expenseId;
        this.description = description;
        this.totalAmount = totalAmount;
        this.paidBy = paidBy;
        this.participants = participants;
        this.splitDetails = splitDetails;
        this.date = date;
    }

    public User getPaidBy() { return paidBy; }
    public Map<User, Double> getSplitDetails() { return splitDetails; }
    public double getTotalAmount() { return totalAmount; }
    public String getExpenseId() { return expenseId; }
    // other getters as needed
}
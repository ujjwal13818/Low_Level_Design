package com.lld.problems.splitwise;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExpenseManager {
    private final BalanceSheet balanceSheet;

    public ExpenseManager(BalanceSheet balanceSheet) {
        this.balanceSheet = balanceSheet;
    }

    public Expense addExpense(Group group, String expenseId, String description, double totalAmount,
                              User paidBy, List<User> participants, SplitStrategy strategy,
                              Optional<Map<User, Double>> inputValues) {

        validateMembership(group, paidBy, participants);

        Map<User, Double> splitDetails = strategy.calculateSplit(totalAmount, participants, inputValues);
        Expense expense = new Expense(expenseId, description, totalAmount, paidBy, participants, splitDetails, LocalDate.now());

        group.addExpense(expense);
        balanceSheet.addExpense(expense);
        return expense;
    }

    public void removeExpense(Group group, Expense expense) {
        group.removeExpense(expense);
        balanceSheet.removeExpense(expense);
    }

    private void validateMembership(Group group, User paidBy, List<User> participants) {
        List<User> members = group.getMembers();

        if (!members.contains(paidBy)) {
            throw new IllegalArgumentException("Payer " + paidBy.getUserName() + " is not a member of this group");
        }

        for (User participant : participants) {
            if (!members.contains(participant)) {
                throw new IllegalArgumentException("Participant " + participant.getUserName() + " is not a member of this group");
            }
        }
    }
}

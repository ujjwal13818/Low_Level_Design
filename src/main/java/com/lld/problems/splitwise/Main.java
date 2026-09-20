package com.lld.problems.splitwise;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        User user1 = new User("ujjwal123", "Ujjwal Kumar", 23);
        User user2 = new User("ujjwal124", "Harsh Kumar", 23);
        User user3 = new User("ujjwal125", "Priyank Kumar", 23);

        Group group1 = new Group("grp123", "First Group", user1);
        group1.addMember(user2);
        group1.addMember(user3);

        BalanceSheet balanceSheet = new BalanceSheet();
        ExpenseManager expenseManager = new ExpenseManager(balanceSheet);

        // Equal split: user1 pays 300, split equally among all 3
        expenseManager.addExpense(group1, "exp1", "Dinner", 300.0, user1,
                List.of(user1, user2, user3), new EqualSplitStrategy(), Optional.empty());

        // Exact split: user2 pays 200, exact amounts given
        Map<User, Double> exactSplit = Map.of(user1, 80.0, user2, 70.0, user3, 50.0);
        expenseManager.addExpense(group1, "exp2", "Movie tickets", 200.0, user2,
                List.of(user1, user2, user3), new ExactSplitStrategy(), Optional.of(exactSplit));

        System.out.println("Harsh(user2) owes Ujjwal(user1): " + balanceSheet.getBalance(user2, user1));
        System.out.println("Ujjwal(user1) owes Harsh(user2): " + balanceSheet.getBalance(user1, user2));
        System.out.println("Priyank(user3) owes Ujjwal(user1): " + balanceSheet.getBalance(user3, user1));
    }
}
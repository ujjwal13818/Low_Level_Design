package com.lld.problems.splitwise;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private final String groupId;
    private final String groupName;
    private User admin;
    private final List<User> members;
    private final List<Expense> expenses;

    public Group(String groupId, String groupName, User admin) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.admin = admin;
        this.members = new ArrayList<>();
        this.expenses = new ArrayList<>();
        members.add(admin);
    }

    public void addMember(User user) {
        members.add(user);
    }

    public void removeMember(User user) {
        members.remove(user);
    }

    // default specifier — only ExpenseManager (same package) should call these,
    // so that BalanceSheet always stays in sync with this list
    void addExpense(Expense expense) {
        expenses.add(expense);
    }

    void removeExpense(Expense expense) {
        expenses.remove(expense);
    }

    public String getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public User getAdmin() { return admin; }
    public void setAdmin(User admin) { this.admin = admin; }
    public List<User> getMembers() { return members; }
    public List<Expense> getExpenses() { return expenses; }
}
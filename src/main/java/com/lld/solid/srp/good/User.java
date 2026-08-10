package com.lld.solid.srp.good;

// 1. Just holds data for a single user
public class User {
    private final String userName;
    private final String password;
    private final String name;

    public User(String userName, String password, String name) {
        this.userName = userName;
        this.password = password;
        this.name = name;
    }

    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getName() { return name; }
}
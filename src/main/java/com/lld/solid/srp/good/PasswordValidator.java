package com.lld.solid.srp.good;

// 2. Only responsibility: validate a password
public class PasswordValidator {
    public boolean isValid(String password) {
        return password.length() >= 5 && password.length() <= 10;
    }
}
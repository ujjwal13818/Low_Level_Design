package com.lld.solid.ocp.good;

public class SpecialCharacterRule implements PasswordRule {

    @Override
    public boolean isSatisfiedBy(String password) {
        return password.matches(".*[!@#$%^&*()].*");
    }
}

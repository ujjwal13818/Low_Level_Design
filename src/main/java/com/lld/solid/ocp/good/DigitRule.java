package com.lld.solid.ocp.good;

public class DigitRule implements PasswordRule {

    @Override
    public boolean isSatisfiedBy(String password) {
        return password.matches(".*\\d.*");
    }
}

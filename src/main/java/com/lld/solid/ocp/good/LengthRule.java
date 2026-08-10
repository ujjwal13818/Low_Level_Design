package com.lld.solid.ocp.good;

public class LengthRule implements PasswordRule{

    @Override
    public boolean isSatisfiedBy(String password) {
        return password.length() >= 5 && password.length() <= 10;
    }

}

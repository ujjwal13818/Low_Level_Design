package com.lld.solid.ocp.good;

public interface PasswordRule {
    boolean isSatisfiedBy(String password);
}

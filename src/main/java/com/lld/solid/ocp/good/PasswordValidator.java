package com.lld.solid.ocp.good;

import java.util.List;

public class PasswordValidator {
    private final List<PasswordRule> rules;

    public PasswordValidator(List<PasswordRule> rules) {
        this.rules = rules;
    }

    public boolean isValid(String password) {
        for (PasswordRule rule : rules) {
            if (!rule.isSatisfiedBy(password)) {
                return false; // one failure = whole password invalid, stop immediately
            }
        }
        return true; // survived every rule
    }

    public static void main(String[] args) {
        List<PasswordRule> rules = List.of(
                new LengthRule(),
                new DigitRule(),
                new SpecialCharacterRule()
        );

        PasswordValidator validator = new PasswordValidator(rules);

        System.out.println(validator.isValid("abc12!"));   // true
        System.out.println(validator.isValid("abcdef"));   // false - no digit, no special char
        System.out.println(validator.isValid("a1!"));       // false - too short
    }
}
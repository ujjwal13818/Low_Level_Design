package com.lld.solid.ocp.bad;

public class PasswordValidator {
    public boolean isValid(String password) {
        if (password.length() < 5 || password.length() > 10) return false;
        if (!password.matches(".*\\d.*")) return false; // added later
        return true;
    }
}
//Open/Closed Principle (OCP)
//
//Definition: A class should be open for extension, but closed for modification. Meaning:
// when a new requirement comes in, you should be able to add new code, not edit existing, already-tested code.
//
//Why it matters:
// Every time you modify a class that's already working,
// you risk breaking it and you have to re-test it. If instead you just add a new class, the old code stays untouched and safe.

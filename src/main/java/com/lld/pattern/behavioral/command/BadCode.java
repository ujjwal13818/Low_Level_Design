package com.lld.pattern.behavioral.command;

public class BadCode {
    public void logSet(String exercise, int reps, int weight) {
        System.out.println("Logged: " + exercise + " " + reps + "x" + weight);
        // no way to undo this, no way to queue it, no way to log "what happened" as data
    }
}

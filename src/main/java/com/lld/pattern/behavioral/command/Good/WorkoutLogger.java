package com.lld.pattern.behavioral.command.Good;

public class WorkoutLogger {
    public void logSet(String exercise, int reps, int weight) {
        System.out.println("Logged: " + exercise + " " + reps + "x" + weight);
    }
    public void removeLastSet(String exercise) {
        System.out.println("Removed last set for " + exercise);
    }
}
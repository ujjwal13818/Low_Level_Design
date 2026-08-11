package com.lld.pattern.behavioral.state.good;

public class CompletedState implements SessionState {
    @Override
    public void start(WorkoutSession session) {
        System.out.println("Workout session is already completed. Cannot start.");
    }

    @Override
    public void pause(WorkoutSession session) {
        System.out.println("Workout session is already completed. Cannot pause.");
    }

    @Override
    public void complete(WorkoutSession session) {
        System.out.println("Workout session is already completed.");
    }
}

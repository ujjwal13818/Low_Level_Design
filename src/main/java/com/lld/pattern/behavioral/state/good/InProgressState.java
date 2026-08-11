package com.lld.pattern.behavioral.state.good;

public class InProgressState implements SessionState {
    public void start(WorkoutSession session) {
        System.out.println("Already in progress");
    }
    public void pause(WorkoutSession session) {
        System.out.println("Workout paused");
        session.setState(new PausedState());
    }
    public void complete(WorkoutSession session) {
        System.out.println("Workout completed");
        session.setState(new CompletedState());
    }
}
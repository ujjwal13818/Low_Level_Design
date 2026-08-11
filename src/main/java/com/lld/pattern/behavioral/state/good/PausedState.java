package com.lld.pattern.behavioral.state.good;

public class PausedState implements SessionState {
    @Override
    public void start(WorkoutSession session) {
        System.out.println("Resuming the workout session.");
        session.setState(new InProgressState());
    }

    @Override
    public void pause(WorkoutSession session) {
        System.out.println("Workout is already paused.");
    }

    @Override
    public void complete(WorkoutSession session) {
        System.out.println("Completing the workout session.");
        session.setState(new CompletedState());
    }
}

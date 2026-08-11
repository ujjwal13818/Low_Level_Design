package com.lld.pattern.behavioral.state.good;

public class NotStartedState implements SessionState {
    @Override
    public void start(WorkoutSession session) {
        System.out.println("Starting the workout session.");
        session.setState(new InProgressState());
    }

    @Override
    public void pause(WorkoutSession session) {
        System.out.println("Cannot pause. The workout session hasn't started yet.");
    }

    @Override
    public void complete(WorkoutSession session) {
        System.out.println("Cannot complete. The workout session hasn't started yet.");
    }
}

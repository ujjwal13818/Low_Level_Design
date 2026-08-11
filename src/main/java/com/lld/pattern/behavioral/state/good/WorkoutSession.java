package com.lld.pattern.behavioral.state.good;

public class WorkoutSession {
    private SessionState currentState = new NotStartedState();

    public void setState(SessionState state) {
        this.currentState = state;
    }

    public void start() { currentState.start(this); }
    public void pause() { currentState.pause(this); }
    public void complete() { currentState.complete(this); }

    public static void main(String[] args) {
        WorkoutSession session = new WorkoutSession();
        session.start();
        session.pause();
        session.complete();
        session.setState(new InProgressState());
        session.start();
        session.pause();
        session.complete();
    }
}
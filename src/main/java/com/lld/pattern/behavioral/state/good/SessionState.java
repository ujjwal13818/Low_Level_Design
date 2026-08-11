package com.lld.pattern.behavioral.state.good;

public interface SessionState {
    void start(WorkoutSession session);
    void pause(WorkoutSession session);
    void complete(WorkoutSession session);
}

package com.lld.pattern.creational.builder.bad;

public class WorkoutSession {
    private String userId;
    private String exerciseId;
    private Integer targetReps;
    private Double targetWeight;
    private String notes;

//    public WorkoutSession(String userId, String exerciseId) { ... }
//    public WorkoutSession(String userId, String exerciseId, Integer targetReps) { ... }
//    public WorkoutSession(String userId, String exerciseId, Integer targetReps, Double targetWeight) { ... }
    // ...and so on, exploding for every combination of optional fields
}

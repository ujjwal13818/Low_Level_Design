package com.lld.pattern.creational.builder.good;

public class WorkoutSession {
    private final String userId;
    private final String exerciseId;
    private final Integer targetReps;
    private final Double targetWeight;
    private final String notes;

    // private constructor — only the Builder can call it
    private WorkoutSession(Builder builder) {
        this.userId = builder.userId;
        this.exerciseId = builder.exerciseId;
        this.targetReps = builder.targetReps;
        this.targetWeight = builder.targetWeight;
        this.notes = builder.notes;
    }

    public static class Builder {
        private String userId;
        private String exerciseId;
        private Integer targetReps;
        private Double targetWeight;
        private String notes;

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder exerciseId(String exerciseId) { this.exerciseId = exerciseId; return this; }
        public Builder targetReps(Integer targetReps) { this.targetReps = targetReps; return this; }
        public Builder targetWeight(Double targetWeight) { this.targetWeight = targetWeight; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }

        public WorkoutSession build() {
            return new WorkoutSession(this);
        }
    }
}
package com.lld.pattern.creational.factory.good;

public class ExerciseFactory {
    public static Exercise createExercise(String name) {
        switch (name) {
            case "strength":
                return new StrengthExercise();
            case "cardio":
                return new CardioExercise();
            case "flexibility":
                return new FlexibilityExercise();
            default:
                throw new IllegalArgumentException("Unknown exercise type: " + name);
        }
    }
}

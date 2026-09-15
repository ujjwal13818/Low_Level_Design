package com.lld.pattern.creational.factory.good;

public class SelectExercise {
   public static void main(String[] args) {
       ExerciseFactory.createExercise("strength").describe();
       ExerciseFactory.createExercise("cardio").describe();
       ExerciseFactory.createExercise("flexibility").describe();
   }
}

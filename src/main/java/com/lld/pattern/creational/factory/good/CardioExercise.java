package com.lld.pattern.creational.factory.good;

public class CardioExercise implements Exercise {
    @Override
    public void describe() {
        System.out.println("This is a cardio exercise.");
    }
}

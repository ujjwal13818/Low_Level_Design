package com.lld.pattern.structural.facade.bad;

public class WorkoutController {
//    public void endWorkout(String sessionId) {
//        SessionRepository repo = new SessionRepository();
//        repo.save(sessionId);
//
//        CalorieCalculator calorieCalc = new CalorieCalculator();
//        double calories = calorieCalc.calculate(sessionId);
//
//        StreakService streakService = new StreakService();
//        streakService.updateStreak(sessionId);
//
//        EventPublisher publisher = new KafkaEventPublisher();
//        publisher.publish("workout.completed", sessionId);
//
//        AiRecommendationService aiService = new AiRecommendationService();
//        aiService.generateRecommendation(sessionId);
//
//        System.out.println("Workout ended: " + sessionId + ", calories: " + calories);
  //  }

    //this endworkout is calling so many different methods and services and all the services are exposed which is not necesary to know.
}

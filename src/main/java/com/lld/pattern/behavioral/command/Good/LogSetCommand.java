package com.lld.pattern.behavioral.command.Good;

public class LogSetCommand implements Command {
    private final WorkoutLogger logger;
    private final String exercise;
    private final int reps;
    private final int weight;

    public LogSetCommand(WorkoutLogger logger, String exercise, int reps, int weight) {
        this.logger = logger;
        this.exercise = exercise;
        this.reps = reps;
        this.weight = weight;
    }

    public void execute() {
        logger.logSet(exercise, reps, weight);
    }

    public void undo() {
        logger.removeLastSet(exercise);
        System.out.println("Undid log for " + exercise);
    }
}

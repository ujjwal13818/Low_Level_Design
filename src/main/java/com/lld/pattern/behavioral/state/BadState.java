public class BadState {
    private String state = "NOT_STARTED";

    public void start() {
        if (state.equals("NOT_STARTED")) {
            state = "IN_PROGRESS";
            System.out.println("Workout started");
        } else {
            System.out.println("Cannot start from state: " + state);
        }
    }

    public void pause() {
        if (state.equals("IN_PROGRESS")) {
            state = "PAUSED";
            System.out.println("Workout paused");
        } else {
            System.out.println("Cannot pause from state: " + state);
        }
    }

    public void complete() {
        if (state.equals("IN_PROGRESS") || state.equals("PAUSED")) {
            state = "COMPLETED";
            System.out.println("Workout completed");
        } else {
            System.out.println("Cannot complete from state: " + state);
        }
    }
}
package com.lld.solid.srp.good;

// Orchestrates the flow — this is a "service" layer, coordinates the others
public class UserRegistrationService {
    private final PasswordValidator passwordValidator = new PasswordValidator();
    private final NotificationService notificationService = new NotificationService();
    private final UserRepository userRepository = new UserRepository();

    public void register(String userName, String password, String name) {
        if (!passwordValidator.isValid(password)) {
            System.out.println("Invalid password for " + userName);
            return;
        }
        User user = new User(userName, password, name);
        userRepository.save(user);
        notificationService.sendWelcomeNotification(userName);
    }

    public static void main(String[] args) {
        UserRegistrationService service = new UserRegistrationService();
        service.register("uk4c", "pass1234", "Ujjwal Kumar");
        service.register("baduser", "ab", "Bad User"); // fails validation
    }
}
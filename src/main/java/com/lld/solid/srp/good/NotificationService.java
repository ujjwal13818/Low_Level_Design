package com.lld.solid.srp.good;

// 3. Only responsibility: send notifications
public class NotificationService {
    public void sendWelcomeNotification(String userName) {
        System.out.println("Sending notification to " + userName);
    }
}
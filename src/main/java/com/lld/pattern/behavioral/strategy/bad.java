package com.lld.pattern.behavioral.strategy;

public class bad {
    // In this example, we have a PaymentProcessor class that handles different payment methods. However, the processPayment method is tightly coupled with the payment method implementation, making it difficult to add new payment methods without modifying the existing code. This violates the Open/Closed Principle.

    public void processPayment(String paymentMethod, double amount) {
        if (paymentMethod.equals("CreditCard")) {
            // Process credit card payment
            System.out.println("Processing credit card payment of $" + amount);
        } else if (paymentMethod.equals("PayPal")) {
            // Process PayPal payment
            System.out.println("Processing PayPal payment of $" + amount);
        } else if (paymentMethod.equals("Bitcoin")) {
            // Process Bitcoin payment
            System.out.println("Processing Bitcoin payment of $" + amount);
        } else {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
    }
}

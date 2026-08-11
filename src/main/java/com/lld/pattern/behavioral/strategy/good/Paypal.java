package com.lld.pattern.behavioral.strategy.good;

public class Paypal implements PaymentStrategy {
    @Override
    public void pay(Double amount) {
        System.out.println("Payment of " + amount + " processed via PayPal");
    }
}

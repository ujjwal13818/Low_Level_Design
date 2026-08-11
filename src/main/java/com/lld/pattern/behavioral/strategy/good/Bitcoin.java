package com.lld.pattern.behavioral.strategy.good;

public class Bitcoin implements PaymentStrategy {
    @Override
    public void pay(Double amount) {
        System.out.println("Payment of " + amount + " processed via Bitcoin");
    }
}

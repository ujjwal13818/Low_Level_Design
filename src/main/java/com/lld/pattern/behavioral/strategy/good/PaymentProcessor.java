package com.lld.pattern.behavioral.strategy.good;

public class PaymentProcessor {
    private final PaymentStrategy paymentStrategy;

    public PaymentProcessor(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public void pay(Double amount) {
        paymentStrategy.pay(amount);
    }

    public static void main(String[] args) {
        PaymentProcessor creditCardProcessor = new PaymentProcessor(new CreditCard());
        creditCardProcessor.pay(100.0);
    }
}

package com.lld.pattern.structural.adapter.good;

public class PaymentGatewayAdapter implements PaymentGateway {

    private OldPaymentSystem oldPaymentSystem;

    public PaymentGatewayAdapter(OldPaymentSystem oldPaymentSystem) {
        this.oldPaymentSystem = oldPaymentSystem;
    }

    @Override
    public void processPayment (double amount){
        String amountInCents = String.valueOf((int) (amount * 100));
        oldPaymentSystem.makeTransaction(amountInCents);
    }

}

package com.lld.pattern.structural.adapter.good;

public class PaymentService {
    public static void main(String[] args) {
        PaymentGateway adapter = new PaymentGatewayAdapter(new OldPaymentSystem());
        adapter.processPayment(100.0);
    }
}

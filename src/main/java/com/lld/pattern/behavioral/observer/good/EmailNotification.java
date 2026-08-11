package com.lld.pattern.behavioral.observer.good;

public class EmailNotification implements PriceObserver {
    @Override
    public void onPriceChange(Double newPrice) {
        if(newPrice > 100.0)System.out.println("Email Notification: Stock price updated to " + newPrice);
    }
}

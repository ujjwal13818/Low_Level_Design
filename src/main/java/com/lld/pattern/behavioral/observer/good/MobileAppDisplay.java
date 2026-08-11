package com.lld.pattern.behavioral.observer.good;

public class MobileAppDisplay implements PriceObserver {
    @Override
    public void onPriceChange(Double newPrice) {
        System.out.println("Mobile App Display: Stock price updated to " + newPrice);
    }
}

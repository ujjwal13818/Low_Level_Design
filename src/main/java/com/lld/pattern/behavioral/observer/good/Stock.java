package com.lld.pattern.behavioral.observer.good;

import java.util.ArrayList;
import java.util.List;

public class Stock {
    List<PriceObserver> observers = new ArrayList<>();
    public void addObserver(PriceObserver observer) {
        observers.add(observer);
    }
    public void setPrice(Double price) {
        for (PriceObserver observer : observers) {
            observer.onPriceChange(price);
        }
    }

    public static void main(String[] args) {
        Stock stock = new Stock();
        stock.addObserver(new MobileAppDisplay());
        stock.addObserver(new EmailNotification());

        stock.setPrice(5.0);
    }
}

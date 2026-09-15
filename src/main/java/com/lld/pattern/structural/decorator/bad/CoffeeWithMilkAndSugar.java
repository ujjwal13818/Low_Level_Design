package com.lld.pattern.structural.decorator.bad;

public class CoffeeWithMilkAndSugar extends BasicCoffee {
    public double cost() { return 50.0 + 10.0 + 5.0; }
}
// what about milk+sugar+whippedCream? Another subclass. Every combination = new class.
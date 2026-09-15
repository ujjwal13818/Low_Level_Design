package com.lld.pattern.structural.decorator.good;

public class CheeseTopping extends PizzaDecorator{
    public CheeseTopping(Pizza wrappedPizza) {
        super(wrappedPizza);
    }
    public double cost() {
        return wrappedPizza.cost() + 40.00;
    }
    public String description() {
        return wrappedPizza.description() + ", Cheese";
    }
}

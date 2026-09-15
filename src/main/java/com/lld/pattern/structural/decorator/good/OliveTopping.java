package com.lld.pattern.structural.decorator.good;

public class OliveTopping extends PizzaDecorator{
    public OliveTopping(Pizza wrappedPizza) {
        super(wrappedPizza);
    }
    public double cost() {
        return wrappedPizza.cost() + 25.00;
    }
    public String description() {
        return wrappedPizza.description() + ", Olive";
    }
}

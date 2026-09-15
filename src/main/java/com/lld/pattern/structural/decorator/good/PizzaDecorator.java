package com.lld.pattern.structural.decorator.good;

public abstract class PizzaDecorator implements Pizza{
    public final Pizza wrappedPizza;
    public PizzaDecorator(Pizza wrappedPizza) {
        this.wrappedPizza = wrappedPizza;
    }
}

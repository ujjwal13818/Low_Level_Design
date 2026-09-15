package com.lld.pattern.structural.decorator.good;

public class MakePizza {
    public static void main(String[] args) {
        Pizza completePizza = new OliveTopping(new CheeseTopping(new PlainPizza()));
        System.out.println("Pizza Description: " + completePizza.description());
        System.out.println("Pizza Cost: " + completePizza.cost());
    }
}

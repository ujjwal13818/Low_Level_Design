package com.lld.pattern.structural.decorator.good;

public class PlainPizza implements Pizza{
    public double cost() {
        return 150.00;
    }
    public String description() {
        return "Plain Pizza";
    }
}

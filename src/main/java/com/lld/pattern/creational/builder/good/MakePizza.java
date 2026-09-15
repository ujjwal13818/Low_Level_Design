package com.lld.pattern.creational.builder.good;

public class MakePizza {
    public static void main(String[] args) {
        Pizza pizza1 = new Pizza.Builder()
                .setSize("Large")
                .setCheese(true)
                .setPepporoni(true)
                .setExtraSauce(false)
                .build();

        Pizza pizza2 = new Pizza.Builder()
                .setSize("Medium")
                .setCheese(false)
                .setPepporoni(true)
                .setExtraSauce(true)
                .build();

        pizza1.printPizza();
        pizza2.printPizza();
    }
}

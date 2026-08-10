package com.lld.solid.lsp.bad;

public class Penguin extends Bird{
    @Override
    public void fly() {
        System.out.println("Penguins cannot fly");
    }
}

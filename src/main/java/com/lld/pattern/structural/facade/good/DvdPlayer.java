package com.lld.pattern.structural.facade.good;

public class DvdPlayer {
    public void play(String movie) {
        System.out.println("DVD is playing " + movie);
    }
    public void stop() {
        System.out.println("DVD is stopped");
    }
}

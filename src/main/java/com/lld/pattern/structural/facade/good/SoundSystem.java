package com.lld.pattern.structural.facade.good;

public class SoundSystem {
    public void turnOn() {
        System.out.println("System is turned on");
    }
    public void turnOff() {
        System.out.println("System is turned off");
    }
    public void setVolume(double volume) {
        System.out.println("System is set volume " + volume);
    }
}

package com.lld.pattern.structural.facade.good;

public class HomeTheatreFacade {
    private Projecter projecter = new Projecter();
    private SoundSystem soundSystem = new SoundSystem();
    private DvdPlayer dvdPlayer = new DvdPlayer();

    public void watchMovie(String movie) {
        projecter.turnOn();
        soundSystem.turnOn();
        soundSystem.setVolume(50);
        dvdPlayer.play(movie);
    }

    public void endMovie() {
        projecter.turnOff();
        soundSystem.turnOff();
        dvdPlayer.stop();
    }
}

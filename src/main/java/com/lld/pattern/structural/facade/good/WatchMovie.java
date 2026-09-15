package com.lld.pattern.structural.facade.good;

public class WatchMovie {
    public static void main(String args[]) {
        HomeTheatreFacade homeTheatreFacade = new HomeTheatreFacade();
        homeTheatreFacade.watchMovie("Inception");
        homeTheatreFacade.endMovie();
    }
}

package com.lld.pattern.creational.singleton.good;

public class Logger {
    private static Logger logger;
    private Logger() {
        System.out.println("Loading logger");
    }
    public static Logger getLogger() {
        if(logger == null) {
            logger = new Logger();
        }
        return logger;
    }
}

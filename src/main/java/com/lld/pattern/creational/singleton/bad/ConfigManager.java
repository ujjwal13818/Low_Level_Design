package com.lld.pattern.creational.singleton.bad;

public class ConfigManager {
    public ConfigManager() { System.out.println("Loading config from disk..."); }
}

//mulitple instances can be created
//ConfigManager c1 = new ConfigManager(); // loads config
//ConfigManager c2 = new ConfigManager(); // loads config AGAIN — wasteful, and now two different config states could exist

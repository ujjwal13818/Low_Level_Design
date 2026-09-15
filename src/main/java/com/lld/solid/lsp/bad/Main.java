package com.lld.solid.lsp.bad;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void migrateBirds(List<Bird> birds) {
        for (Bird bird : birds) {
            bird.fly();
            System.out.println("Traveled 500 miles south");
        }
    }

    public static void main(String[] args) {
        List<Bird> birds = new ArrayList<>();
        birds.add(new Bird());
        birds.add(new Bird());
        birds.add(new Penguin());

        migrateBirds(birds);
    }

}

//LSP = Liskov Substitution Principle (LSP)
//The problem isn't that Penguin overrode fly() differently — overriding is normal.
//The problem is what Bird promised, and Penguin broke that promise while still compiling cleanly.
//Here's the core idea of LSP: when you write class Penguin extends Bird, you're not just reusing code —
//you're making a contract claim: "Anywhere in this program that expects a Bird, you can safely hand it a Penguin instead,
//and everything will still behave correctly."
//in Bird, you said bird can fly but in penguin you said penguin can not fly.
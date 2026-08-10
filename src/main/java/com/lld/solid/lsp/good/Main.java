package com.lld.solid.lsp.good;


import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void migrateBirds(List<FlyingBird> birds) {
        for (FlyingBird bird : birds) {
            bird.fly();
            System.out.println("Traveled 500 miles south");
        }
    }

    public static void main(String[] args) {
        List<FlyingBird> birds = new ArrayList<>();
        birds.add(new Sparrow());
        // birds.add(new Penguin()); // This would cause a compile-time error since Penguin doesn't implement FlyingBird and
        // it should have done because penguin can not fly so it should give compile time error
        // instead of silently implementing it.

        migrateBirds(birds);
    }
}

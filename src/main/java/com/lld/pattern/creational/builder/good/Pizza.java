package com.lld.pattern.creational.builder.good;

public class Pizza {
    private String size;
    private boolean cheese;
    private boolean pepporoni;
    private boolean extraSauce;

    private Pizza(Builder builder) {
        this.size = builder.size;
        this.cheese = builder.cheese;
        this.pepporoni = builder.pepporoni;
        this.extraSauce = builder.extraSauce;
    }

    public static class Builder {
        private String size;
        private boolean cheese;
        private boolean pepporoni;
        private boolean extraSauce;

        public Builder setSize(String size) { this.size = size; return this; }
        public Builder setCheese(boolean cheese) { this.cheese = cheese; return this; }
        public Builder setPepporoni(boolean pepporoni) { this.pepporoni = pepporoni; return this; }
        public Builder setExtraSauce(boolean extraSauce) { this.extraSauce = extraSauce; return this; }

        public Pizza build() {
            if(size == null) {
                throw new IllegalStateException("Size must be set");
            }

            return new Pizza(this);
        }
    }

    public void printPizza() {
        System.out.println(size + " " + cheese + " " + pepporoni + " " + extraSauce);
    }
}
package com.example.recipebook.model;

public class Flags {
    private boolean vegan;
    private boolean glutenFree;
    private boolean sugarFree;

    public Flags() {
    }

    public Flags(boolean vegan, boolean glutenFree, boolean sugarFree) {
        this.vegan = vegan;
        this.glutenFree = glutenFree;
        this.sugarFree = sugarFree;
    }

    public boolean isVegan() {
        return vegan;
    }

    public void setVegan(boolean vegan) {
        this.vegan = vegan;
    }

    public boolean isGlutenFree() {
        return glutenFree;
    }

    public void setGlutenFree(boolean glutenFree) {
        this.glutenFree = glutenFree;
    }

    public boolean isSugarFree() {
        return sugarFree;
    }

    public void setSugarFree(boolean sugarFree) {
        this.sugarFree = sugarFree;
    }

    public boolean getByKey(String key) {
        return switch (key) {
            case "vegan" -> vegan;
            case "glutenFree" -> glutenFree;
            case "sugarFree" -> sugarFree;
            default -> false;
        };
    }

    public void setByKey(String key, boolean value) {
        switch (key) {
            case "vegan" -> vegan = value;
            case "glutenFree" -> glutenFree = value;
            case "sugarFree" -> sugarFree = value;
            default -> { }
        }
    }
}

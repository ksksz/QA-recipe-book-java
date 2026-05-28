package com.example.recipebook.model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private String id;
    private String name;
    private List<String> photos = new ArrayList<>();
    private double calories;
    private double proteins;
    private double fats;
    private double carbs;
    private String compositionText;
    private String category;
    private String cookingNeed;
    private Flags flags = new Flags();
    private String createdAt;
    private String updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos == null ? new ArrayList<>() : photos;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProteins() {
        return proteins;
    }

    public void setProteins(double proteins) {
        this.proteins = proteins;
    }

    public double getFats() {
        return fats;
    }

    public void setFats(double fats) {
        this.fats = fats;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public String getCompositionText() {
        return compositionText;
    }

    public void setCompositionText(String compositionText) {
        this.compositionText = compositionText;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCookingNeed() {
        return cookingNeed;
    }

    public void setCookingNeed(String cookingNeed) {
        this.cookingNeed = cookingNeed;
    }

    public Flags getFlags() {
        return flags;
    }

    public void setFlags(Flags flags) {
        this.flags = flags == null ? new Flags() : flags;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}

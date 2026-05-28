package com.example.recipebook.model;

import java.util.ArrayList;
import java.util.List;

public class Dish {
    private String id;
    private String name;
    private List<String> photos = new ArrayList<>();
    private double calories;
    private double proteins;
    private double fats;
    private double carbs;
    private Nutrition nutritionDraft = new Nutrition();
    private List<Ingredient> composition = new ArrayList<>();
    private double portionSize;
    private String category;
    private Flags flags = new Flags();
    private Flags availableFlags = new Flags();
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

    public Nutrition getNutritionDraft() {
        return nutritionDraft;
    }

    public void setNutritionDraft(Nutrition nutritionDraft) {
        this.nutritionDraft = nutritionDraft == null ? new Nutrition() : nutritionDraft;
    }

    public List<Ingredient> getComposition() {
        return composition;
    }

    public void setComposition(List<Ingredient> composition) {
        this.composition = composition == null ? new ArrayList<>() : composition;
    }

    public double getPortionSize() {
        return portionSize;
    }

    public void setPortionSize(double portionSize) {
        this.portionSize = portionSize;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Flags getFlags() {
        return flags;
    }

    public void setFlags(Flags flags) {
        this.flags = flags == null ? new Flags() : flags;
    }

    public Flags getAvailableFlags() {
        return availableFlags;
    }

    public void setAvailableFlags(Flags availableFlags) {
        this.availableFlags = availableFlags == null ? new Flags() : availableFlags;
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

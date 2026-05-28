package com.example.recipebook.model;

public class Ingredient {
    private String productId;
    private double amount;

    public Ingredient() {
    }

    public Ingredient(String productId, double amount) {
        this.productId = productId;
        this.amount = amount;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}

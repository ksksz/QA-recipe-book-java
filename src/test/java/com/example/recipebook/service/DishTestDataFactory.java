package com.example.recipebook.service;

import com.example.recipebook.model.Flags;
import com.example.recipebook.model.Ingredient;
import com.example.recipebook.model.Product;

public final class DishTestDataFactory {
    private DishTestDataFactory() {
    }

    public static Product product(String id, String name, double calories, double proteins, double fats, double carbs) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setCalories(calories);
        product.setProteins(proteins);
        product.setFats(fats);
        product.setCarbs(carbs);
        product.setCategory("Овощи");
        product.setCookingNeed("Готовый к употреблению");
        product.setFlags(new Flags(true, true, true));
        return product;
    }

    public static Product potato() {
        return product("potato", "Картофель", 77, 2, 0.4, 16.3);
    }

    public static Product water() {
        return product("water", "Вода", 0, 0, 0, 0);
    }

    public static Product meat() {
        return product("meat", "Мясо", 187.2, 18.9, 12.4, 0);
    }

    public static Product beet() {
        return product("beet", "Свёкла", 43, 1.6, 0.2, 9.6);
    }

    public static Ingredient ingredient(String productId, double amount) {
        return new Ingredient(productId, amount);
    }
}

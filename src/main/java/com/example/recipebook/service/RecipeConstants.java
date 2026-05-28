package com.example.recipebook.service;

import java.util.List;
import java.util.Map;

public final class RecipeConstants {
    public static final List<String> PRODUCT_CATEGORIES = List.of(
            "Замороженный", "Мясной", "Овощи", "Зелень", "Специи", "Крупы", "Консервы", "Жидкость", "Сладости"
    );
    public static final List<String> COOKING_NEED = List.of("Готовый к употреблению", "Полуфабрикат", "Требует приготовления");
    public static final List<String> DISH_CATEGORIES = List.of("Десерт", "Первое", "Второе", "Напиток", "Салат", "Суп", "Перекус");
    public static final List<String> FLAGS = List.of("vegan", "glutenFree", "sugarFree");
    public static final int MAX_PHOTOS = 5;
    public static final Map<String, String> MACRO_TO_CATEGORY = Map.of(
            "!десерт", "Десерт",
            "!первое", "Первое",
            "!второе", "Второе",
            "!напиток", "Напиток",
            "!салат", "Салат",
            "!суп", "Суп",
            "!перекус", "Перекус"
    );

    private RecipeConstants() {
    }
}

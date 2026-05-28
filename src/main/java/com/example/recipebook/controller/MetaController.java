package com.example.recipebook.controller;

import com.example.recipebook.service.RecipeConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MetaController {
    @GetMapping("/api/meta")
    public Map<String, Object> meta() {
        return Map.of(
                "PRODUCT_CATEGORIES", RecipeConstants.PRODUCT_CATEGORIES,
                "COOKING_NEED", RecipeConstants.COOKING_NEED,
                "DISH_CATEGORIES", RecipeConstants.DISH_CATEGORIES,
                "FLAGS", RecipeConstants.FLAGS
        );
    }
}

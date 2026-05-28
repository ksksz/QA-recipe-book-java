package com.example.recipebook.dto;

import com.example.recipebook.model.Dish;
import com.example.recipebook.model.Ingredient;
import com.example.recipebook.model.Product;

import java.util.List;

public class DishDetailsResponse extends Dish {
    private List<IngredientDetails> compositionDetailed;

    public List<IngredientDetails> getCompositionDetailed() {
        return compositionDetailed;
    }

    public void setCompositionDetailed(List<IngredientDetails> compositionDetailed) {
        this.compositionDetailed = compositionDetailed;
    }

    public static class IngredientDetails extends Ingredient {
        private Product product;

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }
    }
}

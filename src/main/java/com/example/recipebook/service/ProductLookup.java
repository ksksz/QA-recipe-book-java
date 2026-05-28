package com.example.recipebook.service;

import com.example.recipebook.model.Product;

public interface ProductLookup {
    Product getById(String productId);
}

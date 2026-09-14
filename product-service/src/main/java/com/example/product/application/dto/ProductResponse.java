package com.example.product.application.dto;

import com.example.product.domain.model.Product;

public record ProductResponse(Long id, String name, long price, int stock) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        );
    }
}

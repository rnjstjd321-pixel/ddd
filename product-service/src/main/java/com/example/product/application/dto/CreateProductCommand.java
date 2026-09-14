package com.example.product.application.dto;

public record CreateProductCommand(String name, long price, int stock) {
    public CreateProductCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (price < 0) {
            throw new IllegalArgumentException("price cannot be negative");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("stock cannot be negative");
        }
    }
}

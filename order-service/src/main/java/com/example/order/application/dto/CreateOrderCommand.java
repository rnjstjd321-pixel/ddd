package com.example.order.application.dto;

public record CreateOrderCommand(Long productId, int quantity) {
    public CreateOrderCommand {
        if (productId == null) {
            throw new IllegalArgumentException("productId is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
    }
}

package com.example.product.application.dto;

public record OrderPlacedCommand(
        Long orderId,
        Long productId,
        int quantity,
        long amount,
        String currency
) {
}

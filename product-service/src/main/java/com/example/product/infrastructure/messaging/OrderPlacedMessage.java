package com.example.product.infrastructure.messaging;

import java.time.Instant;

public record OrderPlacedMessage(
        Long orderId,
        Long productId,
        int quantity,
        long amount,
        String currency,
        Instant occurredAt
) {
}

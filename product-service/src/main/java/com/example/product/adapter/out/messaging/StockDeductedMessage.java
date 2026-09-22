package com.example.product.adapter.out.messaging;

import java.time.Instant;

public record StockDeductedMessage(
        String eventId,
        Long orderId,
        Long productId,
        int quantity,
        long amount,
        String currency,
        Instant occurredAt
) {
}

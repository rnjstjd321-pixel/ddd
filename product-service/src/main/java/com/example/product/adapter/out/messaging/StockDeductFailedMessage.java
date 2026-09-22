package com.example.product.adapter.out.messaging;

import java.time.Instant;

public record StockDeductFailedMessage(
        String eventId,
        Long orderId,
        Long productId,
        int quantity,
        String reason,
        Instant occurredAt
) {
}

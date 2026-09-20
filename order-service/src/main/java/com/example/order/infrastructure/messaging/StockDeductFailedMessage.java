package com.example.order.infrastructure.messaging;

import java.time.Instant;

public record StockDeductFailedMessage(
        Long orderId,
        Long productId,
        int quantity,
        String reason,
        Instant occurredAt
) {
}

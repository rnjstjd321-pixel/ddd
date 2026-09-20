package com.example.order.infrastructure.messaging;

import java.time.Instant;

public record OrderCancelledMessage(
        Long orderId,
        Long productId,
        int quantity,
        boolean wasPaid,
        Instant occurredAt
) {
}

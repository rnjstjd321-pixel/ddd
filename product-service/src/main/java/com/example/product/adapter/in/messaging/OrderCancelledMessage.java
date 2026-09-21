package com.example.product.adapter.in.messaging;

import java.time.Instant;

public record OrderCancelledMessage(
        Long orderId,
        Long productId,
        int quantity,
        boolean wasPaid,
        Instant occurredAt
) {
}

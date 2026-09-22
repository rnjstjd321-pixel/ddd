package com.example.order.adapter.out.messaging;

import java.time.Instant;

public record OrderPlacedMessage(
        String eventId,
        Long orderId,
        Long productId,
        int quantity,
        long amount,
        String currency,
        Instant occurredAt
) {
}

package com.example.payment.adapter.in.messaging;

import java.time.Instant;

public record OrderCancelledMessage(
        String eventId,
        Long orderId,
        Long productId,
        int quantity,
        boolean wasPaid,
        Instant occurredAt
) {
}

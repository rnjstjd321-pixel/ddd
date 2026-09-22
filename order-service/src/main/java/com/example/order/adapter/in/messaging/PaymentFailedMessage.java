package com.example.order.adapter.in.messaging;

import java.time.Instant;

public record PaymentFailedMessage(
        String eventId,
        Long orderId,
        String reason,
        Instant occurredAt
) {
}

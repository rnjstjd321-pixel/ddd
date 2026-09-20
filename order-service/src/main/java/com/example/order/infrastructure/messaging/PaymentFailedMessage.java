package com.example.order.infrastructure.messaging;

import java.time.Instant;

public record PaymentFailedMessage(
        Long orderId,
        String reason,
        Instant occurredAt
) {
}

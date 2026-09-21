package com.example.order.adapter.in.messaging;

import java.time.Instant;

public record PaymentFailedMessage(
        Long orderId,
        String reason,
        Instant occurredAt
) {
}

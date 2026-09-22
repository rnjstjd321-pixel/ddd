package com.example.payment.adapter.out.messaging;

import java.time.Instant;

public record PaymentFailedMessage(
        String eventId,
        Long orderId,
        String reason,
        Instant occurredAt
) {
}

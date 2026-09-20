package com.example.order.infrastructure.messaging;

import java.time.Instant;

public record PaymentApprovedMessage(
        Long orderId,
        Long paymentId,
        long amount,
        String status,
        Instant occurredAt
) {
}

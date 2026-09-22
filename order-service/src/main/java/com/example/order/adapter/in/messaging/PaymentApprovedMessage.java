package com.example.order.adapter.in.messaging;

import java.time.Instant;

public record PaymentApprovedMessage(
        String eventId,
        Long orderId,
        Long paymentId,
        long amount,
        String status,
        Instant occurredAt
) {
}

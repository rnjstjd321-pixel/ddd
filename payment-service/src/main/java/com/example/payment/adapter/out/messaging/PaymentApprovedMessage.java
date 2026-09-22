package com.example.payment.adapter.out.messaging;

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

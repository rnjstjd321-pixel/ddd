package com.example.payment.adapter.in.messaging;

import java.time.Instant;

public record StockDeductedMessage(
        Long orderId,
        Long productId,
        int quantity,
        long amount,
        String currency,
        Instant occurredAt
) {
}

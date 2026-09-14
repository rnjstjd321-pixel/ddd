package com.example.payment.application.dto;

import com.example.payment.domain.model.Payment;

public record PaymentResponse(Long id, Long orderId, long amount, String status) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus().name()
        );
    }
}

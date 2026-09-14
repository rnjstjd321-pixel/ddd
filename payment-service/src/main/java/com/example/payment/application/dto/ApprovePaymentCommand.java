package com.example.payment.application.dto;

public record ApprovePaymentCommand(Long orderId, long amount) {
    public ApprovePaymentCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId is required");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
    }
}

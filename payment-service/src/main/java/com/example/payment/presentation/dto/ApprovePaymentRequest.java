package com.example.payment.presentation.dto;

import com.example.payment.application.dto.ApprovePaymentCommand;

public record ApprovePaymentRequest(Long orderId, long amount) {
    public ApprovePaymentCommand toCommand() {
        return new ApprovePaymentCommand(orderId, amount);
    }
}

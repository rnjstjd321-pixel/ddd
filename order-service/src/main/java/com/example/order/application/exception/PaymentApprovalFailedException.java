package com.example.order.application.exception;

public class PaymentApprovalFailedException extends RuntimeException {
    public PaymentApprovalFailedException() {
        super("Payment approval failed");
    }
}

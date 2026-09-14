package com.example.payment.domain.exception;

public class InvalidPaymentStateException extends DomainException {
    public InvalidPaymentStateException(String message) {
        super(message);
    }
}

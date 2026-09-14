package com.example.product.domain.exception;

public class InsufficientStockException extends DomainException {
    public InsufficientStockException() {
        super("Insufficient stock");
    }
}

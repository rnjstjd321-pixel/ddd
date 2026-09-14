package com.example.order.application.exception;

public class ProductUnavailableException extends RuntimeException {
    public ProductUnavailableException(Long productId) {
        super("Product is unavailable: " + productId);
    }
}

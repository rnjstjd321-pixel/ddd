package com.example.order.domain.exception;

public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException(Long orderId) {
        super("Order not found: " + orderId);
    }
}

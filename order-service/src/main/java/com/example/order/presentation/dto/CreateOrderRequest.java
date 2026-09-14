package com.example.order.presentation.dto;

import com.example.order.application.dto.CreateOrderCommand;

public record CreateOrderRequest(Long productId, int quantity) {
    public CreateOrderCommand toCommand() {
        return new CreateOrderCommand(productId, quantity);
    }
}

package com.example.product.presentation.dto;

import com.example.product.application.dto.CreateProductCommand;

public record CreateProductRequest(String name, long price, int stock) {
    public CreateProductCommand toCommand() {
        return new CreateProductCommand(name, price, stock);
    }
}

package com.example.order.application.dto;

import com.example.order.domain.model.Order;
import com.example.order.domain.model.OrderLine;

public record OrderResponse(
        Long id,
        Long productId,
        String productName,
        long unitPrice,
        int quantity,
        long totalPrice,
        String status
) {
    public static OrderResponse from(Order order) {
        OrderLine line = order.firstLine();
        return new OrderResponse(
                order.getId(),
                line.getProductId(),
                line.getProductName(),
                line.getUnitPrice().toLong(),
                line.getQuantity(),
                order.totalAmount().toLong(),
                order.getStatus().name()
        );
    }
}

package com.example.order.adapter.in.web;

import com.example.order.application.dto.OrderResponse;
import com.example.order.application.port.in.OrderUseCase;
import com.example.order.adapter.in.web.dto.CreateOrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound Adapter (HTTP). 웹 요청을 Inbound Port(OrderUseCase)로 전달한다.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderWebAdapter {
    private final OrderUseCase orderApplicationService;

    public OrderWebAdapter(OrderUseCase orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        return orderApplicationService.createOrder(request.toCommand());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderApplicationService.getOrder(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        return orderApplicationService.cancelOrder(id);
    }
}

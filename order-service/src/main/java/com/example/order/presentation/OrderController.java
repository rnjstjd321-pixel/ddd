package com.example.order.presentation;

import com.example.order.application.dto.OrderResponse;
import com.example.order.application.service.OrderApplicationService;
import com.example.order.presentation.dto.CreateOrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP 진입점. 도메인 객체를 직접 다루지 않고 Application Service에 유스케이스를 위임한다.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
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

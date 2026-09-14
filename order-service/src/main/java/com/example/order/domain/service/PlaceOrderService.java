package com.example.order.domain.service;

import com.example.order.domain.event.DomainEventPublisher;
import com.example.order.domain.model.Order;
import com.example.order.domain.model.ProductSnapshot;
import com.example.order.domain.repository.OrderRepository;
import java.util.List;

/**
 * 도메인 서비스(Domain Service).
 * 주문 생성 규칙을 Aggregate에 위임하고, 저장 후 도메인 이벤트를 발행한다.
 */
public class PlaceOrderService {
    private final OrderRepository orderRepository;
    private final DomainEventPublisher domainEventPublisher;

    public PlaceOrderService(OrderRepository orderRepository, DomainEventPublisher domainEventPublisher) {
        this.orderRepository = orderRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public Order place(List<OrderLineRequest> lines) {
        Order order = Order.create();
        lines.forEach(line -> order.addLine(line.snapshot(), line.quantity()));

        Order saved = orderRepository.save(order);
        domainEventPublisher.publish(saved.placedEvent());
        return saved;
    }

    public record OrderLineRequest(ProductSnapshot snapshot, int quantity) {
    }
}

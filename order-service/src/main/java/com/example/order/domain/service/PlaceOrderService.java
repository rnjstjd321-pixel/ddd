package com.example.order.domain.service;

import com.example.order.domain.model.Order;
import com.example.order.domain.model.ProductSnapshot;
import java.util.List;

/**
 * 도메인 서비스(Domain Service).
 * 주문 생성 규칙을 Aggregate에 위임한다. 저장/이벤트 발행은 Application 계층의 책임이다.
 */
public class PlaceOrderService {

    public Order place(List<OrderLineRequest> lines) {
        Order order = Order.create();
        lines.forEach(line -> order.addLine(line.snapshot(), line.quantity()));
        return order;
    }

    public record OrderLineRequest(ProductSnapshot snapshot, int quantity) {
    }
}

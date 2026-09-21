package com.example.order.application.port.in;

import com.example.order.application.dto.CreateOrderCommand;
import com.example.order.application.dto.OrderResponse;

/**
 * Inbound Port.
 * 웹/메시징 Inbound Adapter가 주문 유스케이스를 호출할 때 사용하는 계약이다.
 */
public interface OrderUseCase {
    OrderResponse createOrder(CreateOrderCommand command);

    OrderResponse getOrder(Long orderId);

    OrderResponse cancelOrder(Long orderId);

    void markOrderPaid(Long orderId);

    void failOrderAfterStockFailure(Long orderId);

    void failOrderAfterPaymentFailure(Long orderId);
}

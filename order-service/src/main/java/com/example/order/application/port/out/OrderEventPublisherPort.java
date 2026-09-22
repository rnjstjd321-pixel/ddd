package com.example.order.application.port.out;

import com.example.order.domain.event.OrderCancelled;
import com.example.order.domain.event.OrderPlaced;

/**
 * Outbound Port.
 * 애플리케이션이 도메인 이벤트를 외부(메시지 브로커)로 알릴 때 사용하는 계약이다.
 */
public interface OrderEventPublisherPort {
    void publish(OrderPlaced event);

    void publish(OrderCancelled event);
}

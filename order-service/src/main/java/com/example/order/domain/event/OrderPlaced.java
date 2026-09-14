package com.example.order.domain.event;

import com.example.order.domain.model.Money;
import java.time.Instant;

/**
 * 도메인 이벤트(Domain Event).
 * 주문이 생성된 사실과 총액을 기록해 후속 처리에 사용한다.
 */
public final class OrderPlaced {
    private final Long orderId;
    private final Money total;
    private final Instant occurredAt = Instant.now();

    public OrderPlaced(Long orderId, Money total) {
        this.orderId = orderId;
        this.total = total;
    }

    public Long orderId() {
        return orderId;
    }

    public Money total() {
        return total;
    }

    public Instant occurredAt() {
        return occurredAt;
    }
}

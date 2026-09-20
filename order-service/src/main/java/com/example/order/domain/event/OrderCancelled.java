package com.example.order.domain.event;

import java.time.Instant;

/**
 * 도메인 이벤트(Domain Event).
 * 주문이 취소된 사실을 기록해 재고 복구/결제 취소를 유도한다.
 */
public final class OrderCancelled {
    private final Long orderId;
    private final Long productId;
    private final int quantity;
    private final boolean wasPaid;
    private final Instant occurredAt = Instant.now();

    public OrderCancelled(Long orderId, Long productId, int quantity, boolean wasPaid) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.wasPaid = wasPaid;
    }

    public Long orderId() {
        return orderId;
    }

    public Long productId() {
        return productId;
    }

    public int quantity() {
        return quantity;
    }

    public boolean wasPaid() {
        return wasPaid;
    }

    public Instant occurredAt() {
        return occurredAt;
    }
}

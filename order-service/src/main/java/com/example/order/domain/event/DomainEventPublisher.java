package com.example.order.domain.event;

/**
 * 도메인 이벤트 발행 포트.
 * 도메인은 발행 사실만 알고, 실제 Kafka/로그 구현은 infrastructure가 담당한다.
 */
public interface DomainEventPublisher {
    void publish(OrderPlaced event);
}

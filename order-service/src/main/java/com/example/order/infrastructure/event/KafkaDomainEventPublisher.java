package com.example.order.infrastructure.event;

import com.example.order.domain.event.DomainEventPublisher;
import com.example.order.domain.event.OrderCancelled;
import com.example.order.domain.event.OrderPlaced;
import com.example.order.infrastructure.messaging.OrderCancelledMessage;
import com.example.order.infrastructure.messaging.OrderPlacedMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Outbound Adapter (Kafka).
 * 트랜잭션 커밋 이후에 도메인 이벤트를 Kafka로 발행한다.
 */
@Component
public class KafkaDomainEventPublisher implements DomainEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String orderPlacedTopic;
    private final String orderCancelledTopic;

    public KafkaDomainEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.order-placed}") String orderPlacedTopic,
            @Value("${app.kafka.topics.order-cancelled}") String orderCancelledTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.orderPlacedTopic = orderPlacedTopic;
        this.orderCancelledTopic = orderCancelledTopic;
    }

    @Override
    public void publish(OrderPlaced event) {
        OrderPlacedMessage message = new OrderPlacedMessage(
                event.orderId(),
                event.productId(),
                event.quantity(),
                event.total().toLong(),
                event.total().currency(),
                event.occurredAt()
        );
        publishAfterCommit(orderPlacedTopic, event.orderId(), message);
    }

    @Override
    public void publish(OrderCancelled event) {
        OrderCancelledMessage message = new OrderCancelledMessage(
                event.orderId(),
                event.productId(),
                event.quantity(),
                event.wasPaid(),
                event.occurredAt()
        );
        publishAfterCommit(orderCancelledTopic, event.orderId(), message);
    }

    private void publishAfterCommit(String topic, Long orderId, Object payload) {
        Runnable send = () -> {
            try {
                String json = objectMapper.writeValueAsString(payload);
                kafkaTemplate.send(topic, String.valueOf(orderId), json);
                log.info("Published {} orderId={}", topic, orderId);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to serialize kafka payload", e);
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send.run();
                }
            });
            return;
        }
        send.run();
    }
}

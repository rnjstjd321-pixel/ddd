package com.example.product.adapter.out.messaging;

import com.example.product.application.dto.OrderPlacedCommand;
import com.example.product.application.port.out.StockEventPublisherPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Outbound Adapter (Kafka). StockEventPublisherPort 구현.
 */
@Component
public class KafkaStockEventPublisher implements StockEventPublisherPort {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String stockDeductedTopic;
    private final String stockDeductFailedTopic;

    public KafkaStockEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.stock-deducted}") String stockDeductedTopic,
            @Value("${app.kafka.topics.stock-deduct-failed}") String stockDeductFailedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.stockDeductedTopic = stockDeductedTopic;
        this.stockDeductFailedTopic = stockDeductFailedTopic;
    }

    @Override
    public void publishStockDeducted(OrderPlacedCommand command) {
        send(stockDeductedTopic, command.orderId(), new StockDeductedMessage(
                UUID.randomUUID().toString(),
                command.orderId(),
                command.productId(),
                command.quantity(),
                command.amount(),
                command.currency(),
                Instant.now()
        ));
    }

    @Override
    public void publishStockDeductFailed(OrderPlacedCommand command, String reason) {
        send(stockDeductFailedTopic, command.orderId(), new StockDeductFailedMessage(
                UUID.randomUUID().toString(),
                command.orderId(),
                command.productId(),
                command.quantity(),
                reason,
                Instant.now()
        ));
    }

    private void send(String topic, Long orderId, Object payload) {
        try {
            kafkaTemplate.send(topic, String.valueOf(orderId), objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize kafka payload", e);
        }
    }
}

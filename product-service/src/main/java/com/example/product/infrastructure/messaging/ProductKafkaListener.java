package com.example.product.infrastructure.messaging;

import com.example.product.application.service.ProductApplicationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Inbound Adapter (Kafka).
 * 주문 이벤트를 구독해 재고를 차감/복구하고, 결과를 다시 발행한다.
 */
@Component
public class ProductKafkaListener {
    private static final Logger log = LoggerFactory.getLogger(ProductKafkaListener.class);

    private final ProductApplicationService productApplicationService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String stockDeductedTopic;
    private final String stockDeductFailedTopic;

    public ProductKafkaListener(
            ProductApplicationService productApplicationService,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.stock-deducted}") String stockDeductedTopic,
            @Value("${app.kafka.topics.stock-deduct-failed}") String stockDeductFailedTopic) {
        this.productApplicationService = productApplicationService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.stockDeductedTopic = stockDeductedTopic;
        this.stockDeductFailedTopic = stockDeductFailedTopic;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-placed}", groupId = "product-service")
    public void onOrderPlaced(String payload) throws Exception {
        OrderPlacedMessage message = objectMapper.readValue(payload, OrderPlacedMessage.class);
        log.info("Received order.placed orderId={} productId={}", message.orderId(), message.productId());
        try {
            productApplicationService.decreaseProductStock(message.productId(), message.quantity());
            publish(stockDeductedTopic, message.orderId(), new StockDeductedMessage(
                    message.orderId(),
                    message.productId(),
                    message.quantity(),
                    message.amount(),
                    message.currency(),
                    Instant.now()
            ));
        } catch (RuntimeException e) {
            log.error("Stock deduct failed orderId={}", message.orderId(), e);
            publish(stockDeductFailedTopic, message.orderId(), new StockDeductFailedMessage(
                    message.orderId(),
                    message.productId(),
                    message.quantity(),
                    e.getMessage(),
                    Instant.now()
            ));
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.order-cancelled}", groupId = "product-service")
    public void onOrderCancelled(String payload) throws Exception {
        OrderCancelledMessage message = objectMapper.readValue(payload, OrderCancelledMessage.class);
        log.info("Received order.cancelled orderId={} productId={}", message.orderId(), message.productId());
        productApplicationService.increaseProductStock(message.productId(), message.quantity());
    }

    private void publish(String topic, Long orderId, Object payload) {
        try {
            kafkaTemplate.send(topic, String.valueOf(orderId), objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize kafka payload", e);
        }
    }
}

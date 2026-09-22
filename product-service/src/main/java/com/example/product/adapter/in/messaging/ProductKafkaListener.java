package com.example.product.adapter.in.messaging;

import com.example.product.application.dto.OrderPlacedCommand;
import com.example.product.application.port.in.OrderEventUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Inbound Adapter (Kafka).
 * 주문 이벤트를 구독해 Inbound Port(OrderEventUseCase)로 전달한다.
 */
@Component
public class ProductKafkaListener {
    private static final Logger log = LoggerFactory.getLogger(ProductKafkaListener.class);

    private final OrderEventUseCase orderEventUseCase;
    private final ObjectMapper objectMapper;

    public ProductKafkaListener(OrderEventUseCase orderEventUseCase, ObjectMapper objectMapper) {
        this.orderEventUseCase = orderEventUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-placed}", groupId = "product-service")
    public void onOrderPlaced(String payload) throws Exception {
        OrderPlacedMessage message = objectMapper.readValue(payload, OrderPlacedMessage.class);
        log.info("Received order.placed eventId={} orderId={} productId={}", message.eventId(), message.orderId(), message.productId());
        orderEventUseCase.onOrderPlaced(new OrderPlacedCommand(
                message.orderId(),
                message.productId(),
                message.quantity(),
                message.amount(),
                message.currency()
        ));
    }

    @KafkaListener(topics = "${app.kafka.topics.order-cancelled}", groupId = "product-service")
    public void onOrderCancelled(String payload) throws Exception {
        OrderCancelledMessage message = objectMapper.readValue(payload, OrderCancelledMessage.class);
        log.info("Received order.cancelled eventId={} orderId={} productId={}", message.eventId(), message.orderId(), message.productId());
        orderEventUseCase.onOrderCancelled(message.orderId(), message.productId(), message.quantity());
    }
}

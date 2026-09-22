package com.example.payment.adapter.in.messaging;

import com.example.payment.application.port.in.OrderEventUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Inbound Adapter (Kafka).
 * 재고 차감/주문 취소 이벤트를 구독해 Inbound Port(OrderEventUseCase)로 전달한다.
 */
@Component
public class PaymentKafkaListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentKafkaListener.class);

    private final OrderEventUseCase orderEventUseCase;
    private final ObjectMapper objectMapper;

    public PaymentKafkaListener(OrderEventUseCase orderEventUseCase, ObjectMapper objectMapper) {
        this.orderEventUseCase = orderEventUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.stock-deducted}", groupId = "payment-service")
    public void onStockDeducted(String payload) throws Exception {
        StockDeductedMessage message = objectMapper.readValue(payload, StockDeductedMessage.class);
        log.info("Received stock.deducted eventId={} orderId={}", message.eventId(), message.orderId());
        orderEventUseCase.onStockDeducted(message.orderId(), message.amount());
    }

    @KafkaListener(topics = "${app.kafka.topics.order-cancelled}", groupId = "payment-service")
    public void onOrderCancelled(String payload) throws Exception {
        OrderCancelledMessage message = objectMapper.readValue(payload, OrderCancelledMessage.class);
        log.info("Received order.cancelled eventId={} orderId={} wasPaid={}", message.eventId(), message.orderId(), message.wasPaid());
        orderEventUseCase.onOrderCancelled(message.orderId(), message.wasPaid());
    }
}

package com.example.order.adapter.in.messaging;

import com.example.order.application.port.in.OrderUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Inbound Adapter (Kafka).
 * 결제/재고 결과를 구독해 주문 상태를 최종 확정한다.
 */
@Component
public class OrderKafkaListener {
    private static final Logger log = LoggerFactory.getLogger(OrderKafkaListener.class);

    private final OrderUseCase orderApplicationService;
    private final ObjectMapper objectMapper;

    public OrderKafkaListener(OrderUseCase orderApplicationService, ObjectMapper objectMapper) {
        this.orderApplicationService = orderApplicationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-approved}", groupId = "order-service")
    public void onPaymentApproved(String payload) throws Exception {
        PaymentApprovedMessage message = objectMapper.readValue(payload, PaymentApprovedMessage.class);
        log.info("Received payment.approved orderId={}", message.orderId());
        orderApplicationService.markOrderPaid(message.orderId());
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-failed}", groupId = "order-service")
    public void onPaymentFailed(String payload) throws Exception {
        PaymentFailedMessage message = objectMapper.readValue(payload, PaymentFailedMessage.class);
        log.warn("Received payment.failed orderId={} reason={}", message.orderId(), message.reason());
        orderApplicationService.failOrderAfterPaymentFailure(message.orderId());
    }

    @KafkaListener(topics = "${app.kafka.topics.stock-deduct-failed}", groupId = "order-service")
    public void onStockDeductFailed(String payload) throws Exception {
        StockDeductFailedMessage message = objectMapper.readValue(payload, StockDeductFailedMessage.class);
        log.warn("Received stock.deduct.failed orderId={} reason={}", message.orderId(), message.reason());
        orderApplicationService.failOrderAfterStockFailure(message.orderId());
    }
}

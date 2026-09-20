package com.example.payment.adapter.in.messaging;

import com.example.payment.adapter.out.messaging.PaymentApprovedMessage;
import com.example.payment.adapter.out.messaging.PaymentFailedMessage;
import com.example.payment.application.dto.ApprovePaymentCommand;
import com.example.payment.application.dto.PaymentResponse;
import com.example.payment.application.port.in.PaymentUseCase;
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
 * 재고 차감 완료 후 결제를 승인하고, 주문 취소 시 결제를 취소한다.
 */
@Component
public class PaymentKafkaListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentKafkaListener.class);

    private final PaymentUseCase paymentUseCase;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String paymentApprovedTopic;
    private final String paymentFailedTopic;

    public PaymentKafkaListener(
            PaymentUseCase paymentUseCase,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.payment-approved}") String paymentApprovedTopic,
            @Value("${app.kafka.topics.payment-failed}") String paymentFailedTopic) {
        this.paymentUseCase = paymentUseCase;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.paymentApprovedTopic = paymentApprovedTopic;
        this.paymentFailedTopic = paymentFailedTopic;
    }

    @KafkaListener(topics = "${app.kafka.topics.stock-deducted}", groupId = "payment-service")
    public void onStockDeducted(String payload) throws Exception {
        StockDeductedMessage message = objectMapper.readValue(payload, StockDeductedMessage.class);
        log.info("Received stock.deducted orderId={}", message.orderId());
        try {
            PaymentResponse payment = paymentUseCase.createPayment(
                    new ApprovePaymentCommand(message.orderId(), message.amount())
            );
            publish(paymentApprovedTopic, message.orderId(), new PaymentApprovedMessage(
                    payment.orderId(),
                    payment.id(),
                    payment.amount(),
                    payment.status(),
                    Instant.now()
            ));
        } catch (RuntimeException e) {
            log.error("Payment approve failed orderId={}", message.orderId(), e);
            publish(paymentFailedTopic, message.orderId(), new PaymentFailedMessage(
                    message.orderId(),
                    e.getMessage(),
                    Instant.now()
            ));
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.order-cancelled}", groupId = "payment-service")
    public void onOrderCancelled(String payload) throws Exception {
        OrderCancelledMessage message = objectMapper.readValue(payload, OrderCancelledMessage.class);
        log.info("Received order.cancelled orderId={} wasPaid={}", message.orderId(), message.wasPaid());
        if (!message.wasPaid()) {
            return;
        }
        try {
            paymentUseCase.cancelPayment(message.orderId());
        } catch (RuntimeException e) {
            log.warn("Skip payment cancel for orderId={}: {}", message.orderId(), e.getMessage());
        }
    }

    private void publish(String topic, Long orderId, Object payload) {
        try {
            kafkaTemplate.send(topic, String.valueOf(orderId), objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize kafka payload", e);
        }
    }
}

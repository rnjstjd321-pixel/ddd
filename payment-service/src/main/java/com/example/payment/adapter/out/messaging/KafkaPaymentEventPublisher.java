package com.example.payment.adapter.out.messaging;

import com.example.payment.application.dto.PaymentResponse;
import com.example.payment.application.port.out.PaymentEventPublisherPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Outbound Adapter (Kafka). PaymentEventPublisherPort 구현.
 */
@Component
public class KafkaPaymentEventPublisher implements PaymentEventPublisherPort {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String paymentApprovedTopic;
    private final String paymentFailedTopic;

    public KafkaPaymentEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.payment-approved}") String paymentApprovedTopic,
            @Value("${app.kafka.topics.payment-failed}") String paymentFailedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.paymentApprovedTopic = paymentApprovedTopic;
        this.paymentFailedTopic = paymentFailedTopic;
    }

    @Override
    public void publishApproved(PaymentResponse payment) {
        send(paymentApprovedTopic, payment.orderId(), new PaymentApprovedMessage(
                UUID.randomUUID().toString(),
                payment.orderId(),
                payment.id(),
                payment.amount(),
                payment.status(),
                Instant.now()
        ));
    }

    @Override
    public void publishFailed(Long orderId, String reason) {
        send(paymentFailedTopic, orderId, new PaymentFailedMessage(
                UUID.randomUUID().toString(),
                orderId, reason, Instant.now()));
    }

    private void send(String topic, Long orderId, Object payload) {
        try {
            kafkaTemplate.send(topic, String.valueOf(orderId), objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize kafka payload", e);
        }
    }
}

package com.example.order.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic orderPlacedTopic(@Value("${app.kafka.topics.order-placed}") String topic) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic orderCancelledTopic(@Value("${app.kafka.topics.order-cancelled}") String topic) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic stockDeductedTopic(@Value("${app.kafka.topics.stock-deducted}") String topic) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic stockDeductFailedTopic(@Value("${app.kafka.topics.stock-deduct-failed}") String topic) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic paymentApprovedTopic(@Value("${app.kafka.topics.payment-approved}") String topic) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic paymentFailedTopic(@Value("${app.kafka.topics.payment-failed}") String topic) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }
}

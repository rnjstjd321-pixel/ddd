package com.example.order.domain.service;

import com.example.order.domain.event.DomainEventPublisher;
import com.example.order.domain.repository.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlaceOrderServiceConfig {

    @Bean
    PlaceOrderService placeOrderService(
            OrderRepository orderRepository,
            DomainEventPublisher domainEventPublisher) {
        return new PlaceOrderService(orderRepository, domainEventPublisher);
    }
}

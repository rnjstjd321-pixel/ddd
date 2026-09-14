package com.example.order.infrastructure.event;

import com.example.order.domain.event.DomainEventPublisher;
import com.example.order.domain.event.OrderPlaced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingDomainEventPublisher implements DomainEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);

    @Override
    public void publish(OrderPlaced event) {
        log.info("OrderPlaced orderId={} total={} {}",
                event.orderId(),
                event.total().toLong(),
                event.total().currency());
    }
}

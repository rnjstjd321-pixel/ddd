package com.example.order.application.port.out;

import com.example.order.domain.model.Order;
import java.util.Optional;

/**
 * Outbound Port.
 * Aggregate Root(Order) 단위로만 조회/저장한다.
 */
public interface OrderRepositoryPort {
    Order save(Order order);

    Optional<Order> findById(Long id);
}

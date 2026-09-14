package com.example.order.domain.repository;

import com.example.order.domain.model.Order;
import java.util.Optional;

/**
 * 리포지토리(Repository).
 * Aggregate Root(Order) 단위로만 조회/저장한다.
 */
public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(Long id);
}

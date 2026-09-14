package com.example.order.infrastructure.persistence;

import com.example.order.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
}

package com.example.order.adapter.out.persistence;

import com.example.order.domain.model.Order;
import com.example.order.application.port.out.OrderRepositoryPort;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class OrderPersistenceAdapter implements OrderRepositoryPort {
    private final OrderJpaRepository jpaRepository;

    public OrderPersistenceAdapter(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        return jpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id);
    }
}

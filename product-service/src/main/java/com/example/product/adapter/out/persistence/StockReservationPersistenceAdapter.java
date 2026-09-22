package com.example.product.adapter.out.persistence;

import com.example.product.application.port.out.StockReservationRepositoryPort;
import com.example.product.domain.model.StockReservation;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Outbound Adapter (JPA). StockReservationRepositoryPort 구현.
 */
@Repository
public class StockReservationPersistenceAdapter implements StockReservationRepositoryPort {
    private final StockReservationJpaRepository jpaRepository;

    public StockReservationPersistenceAdapter(StockReservationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public StockReservation save(StockReservation reservation) {
        return jpaRepository.save(reservation);
    }

    @Override
    public Optional<StockReservation> findByOrderId(Long orderId) {
        return jpaRepository.findById(orderId);
    }
}

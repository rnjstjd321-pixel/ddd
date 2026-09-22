package com.example.product.adapter.out.persistence;

import com.example.product.domain.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReservationJpaRepository extends JpaRepository<StockReservation, Long> {
}

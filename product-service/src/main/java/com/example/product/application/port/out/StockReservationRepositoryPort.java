package com.example.product.application.port.out;

import com.example.product.domain.model.StockReservation;
import java.util.Optional;

/**
 * Outbound Port.
 * 주문별 재고 차감 기록을 저장/조회한다.
 */
public interface StockReservationRepositoryPort {
    StockReservation save(StockReservation reservation);

    Optional<StockReservation> findByOrderId(Long orderId);
}

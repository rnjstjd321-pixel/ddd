package com.example.payment.domain.repository;

import com.example.payment.domain.model.Payment;
import java.util.Optional;

/**
 * 리포지토리(Repository).
 * Aggregate Root(Payment) 단위로만 조회/저장한다.
 */
public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByOrderId(Long orderId);
}

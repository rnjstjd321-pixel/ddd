package com.example.payment.application.port.out;

import com.example.payment.domain.model.Payment;
import java.util.Optional;

/**
 * Outbound Port.
 * 애플리케이션이 결제 Aggregate를 영속화/조회할 때 사용하는 계약이다.
 */
public interface PaymentRepositoryPort {
    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByOrderId(Long orderId);
}

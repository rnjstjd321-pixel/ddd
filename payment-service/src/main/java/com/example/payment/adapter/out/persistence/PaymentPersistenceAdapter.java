package com.example.payment.adapter.out.persistence;

import com.example.payment.application.port.out.PaymentRepositoryPort;
import com.example.payment.domain.model.Payment;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Outbound Adapter (JPA).
 * Outbound Port를 구현해 영속성 기술을 바깥에 둔다.
 */
@Component
public class PaymentPersistenceAdapter implements PaymentRepositoryPort {
    private final PaymentJpaRepository jpaRepository;

    public PaymentPersistenceAdapter(PaymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        return jpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }
}

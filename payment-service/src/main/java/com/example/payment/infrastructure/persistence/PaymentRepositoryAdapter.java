package com.example.payment.infrastructure.persistence;

import com.example.payment.domain.model.Payment;
import com.example.payment.domain.repository.PaymentRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepository {
    private final PaymentJpaRepository jpaRepository;

    public PaymentRepositoryAdapter(PaymentJpaRepository jpaRepository) {
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

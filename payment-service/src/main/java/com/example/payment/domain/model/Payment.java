package com.example.payment.domain.model;

import com.example.payment.domain.exception.InvalidPaymentStateException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 결제 Aggregate Root.
 * 승인/취소 상태 전이는 이 루트를 통해서만 수행한다.
 */
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount", precision = 19, scale = 2, nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", length = 3, nullable = false))
    })
    private Money amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    protected Payment() {
    }

    public static Payment approve(Long orderId, long amount) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId is required");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.amount = Money.krw(amount);
        payment.status = PaymentStatus.PAID;
        return payment;
    }

    public void cancel() {
        if (status == PaymentStatus.CANCELLED) {
            throw new InvalidPaymentStateException("Payment is already cancelled");
        }
        if (status != PaymentStatus.PAID) {
            throw new InvalidPaymentStateException("Only PAID payments can be cancelled");
        }
        status = PaymentStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public long getAmount() {
        return amount.toLong();
    }

    public PaymentStatus getStatus() {
        return status;
    }
}

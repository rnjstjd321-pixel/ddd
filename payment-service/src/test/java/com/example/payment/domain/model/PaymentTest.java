package com.example.payment.domain.model;

import com.example.payment.domain.exception.InvalidPaymentStateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentTest {

    @Test
    void approvePayment() {
        Payment payment = Payment.approve(1L, 5000000);

        assertEquals(1L, payment.getOrderId());
        assertEquals(5000000, payment.getAmount());
        assertEquals(PaymentStatus.PAID, payment.getStatus());
    }

    @Test
    void cancelPaidPayment() {
        Payment payment = Payment.approve(1L, 5000000);

        payment.cancel();

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
    }

    @Test
    void cannotCancelAlreadyCancelledPayment() {
        Payment payment = Payment.approve(1L, 5000000);
        payment.cancel();

        assertThrows(InvalidPaymentStateException.class, payment::cancel);
    }

    @Test
    void rejectNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class, () -> Payment.approve(1L, 0));
    }
}

package com.example.payment.application.service;

import com.example.payment.application.dto.ApprovePaymentCommand;
import com.example.payment.application.dto.PaymentResponse;
import com.example.payment.domain.exception.PaymentNotFoundException;
import com.example.payment.domain.model.Payment;
import com.example.payment.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 서비스.
 * Payment Aggregate Root를 조회/저장만 하고, 취소 규칙은 payment.cancel()에 위임한다.
 */
@Service
@Transactional
public class PaymentApplicationService {
    private final PaymentRepository paymentRepository;

    public PaymentApplicationService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentResponse createPayment(ApprovePaymentCommand command) {
        Payment payment = Payment.approve(command.orderId(), command.amount());
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        return PaymentResponse.from(payment);
    }

    public PaymentResponse cancelPayment(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + orderId));
        payment.cancel();
        return PaymentResponse.from(paymentRepository.save(payment));
    }
}

package com.example.payment.application.service;

import com.example.payment.application.dto.ApprovePaymentCommand;
import com.example.payment.application.dto.PaymentResponse;
import com.example.payment.application.port.in.PaymentUseCase;
import com.example.payment.application.port.out.PaymentRepositoryPort;
import com.example.payment.domain.exception.PaymentNotFoundException;
import com.example.payment.domain.model.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 서비스(유스케이스 구현).
 * Inbound Port를 구현하고, Outbound Port만 의존한다.
 */
@Service
@Transactional
public class PaymentApplicationService implements PaymentUseCase {
    private final PaymentRepositoryPort paymentRepository;

    public PaymentApplicationService(PaymentRepositoryPort paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentResponse createPayment(ApprovePaymentCommand command) {
        return paymentRepository.findByOrderId(command.orderId())
                .map(PaymentResponse::from)
                .orElseGet(() -> {
                    Payment payment = Payment.approve(command.orderId(), command.amount());
                    return PaymentResponse.from(paymentRepository.save(payment));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        return PaymentResponse.from(payment);
    }

    @Override
    public PaymentResponse cancelPayment(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + orderId));
        payment.cancel();
        return PaymentResponse.from(paymentRepository.save(payment));
    }
}

package com.example.payment.application.service;

import com.example.payment.application.dto.ApprovePaymentCommand;
import com.example.payment.application.dto.PaymentResponse;
import com.example.payment.application.port.in.OrderEventUseCase;
import com.example.payment.application.port.in.PaymentUseCase;
import com.example.payment.application.port.out.PaymentEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 주문/재고 이벤트 처리 유스케이스.
 * 결제 트랜잭션은 PaymentUseCase가 소유하고, 이 서비스는 트랜잭션 밖에서 결과 이벤트만 발행한다.
 */
@Service
public class OrderEventService implements OrderEventUseCase {
    private static final Logger log = LoggerFactory.getLogger(OrderEventService.class);

    private final PaymentUseCase paymentUseCase;
    private final PaymentEventPublisherPort paymentEventPublisher;

    public OrderEventService(PaymentUseCase paymentUseCase, PaymentEventPublisherPort paymentEventPublisher) {
        this.paymentUseCase = paymentUseCase;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    @Override
    public void onStockDeducted(Long orderId, long amount) {
        PaymentResponse payment;
        try {
            payment = paymentUseCase.createPayment(new ApprovePaymentCommand(orderId, amount));
        } catch (RuntimeException e) {
            log.error("Payment approve failed orderId={}", orderId, e);
            paymentEventPublisher.publishFailed(orderId, e.getMessage());
            return;
        }
        paymentEventPublisher.publishApproved(payment);
    }

    @Override
    public void onOrderCancelled(Long orderId, boolean wasPaid) {
        if (!wasPaid) {
            return;
        }
        try {
            paymentUseCase.cancelPayment(orderId);
        } catch (RuntimeException e) {
            log.warn("Skip payment cancel for orderId={}: {}", orderId, e.getMessage());
        }
    }
}

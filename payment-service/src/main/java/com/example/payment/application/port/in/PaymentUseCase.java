package com.example.payment.application.port.in;

import com.example.payment.application.dto.ApprovePaymentCommand;
import com.example.payment.application.dto.PaymentResponse;

/**
 * Inbound Port.
 * 외부(웹 등)가 결제 유스케이스를 호출할 때 사용하는 계약이다.
 */
public interface PaymentUseCase {
    PaymentResponse createPayment(ApprovePaymentCommand command);

    PaymentResponse getPayment(Long paymentId);

    PaymentResponse cancelPayment(Long orderId);
}

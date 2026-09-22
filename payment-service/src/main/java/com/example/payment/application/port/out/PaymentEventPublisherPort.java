package com.example.payment.application.port.out;

import com.example.payment.application.dto.PaymentResponse;

/**
 * Outbound Port.
 * 결제 처리 결과를 외부(메시지 브로커)로 알릴 때 사용하는 계약이다.
 */
public interface PaymentEventPublisherPort {
    void publishApproved(PaymentResponse payment);

    void publishFailed(Long orderId, String reason);
}

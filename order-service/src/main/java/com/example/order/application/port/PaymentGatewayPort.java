package com.example.order.application.port;

public interface PaymentGatewayPort {
    PaymentResult approve(Long orderId, long amount);

    void cancelByOrderId(Long orderId);

    record PaymentResult(Long id, Long orderId, long amount, String status) {
        public boolean isPaid() {
            return "PAID".equals(status);
        }
    }
}

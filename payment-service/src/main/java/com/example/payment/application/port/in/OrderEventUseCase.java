package com.example.payment.application.port.in;

/**
 * Inbound Port.
 * 다른 Context(Product, Order)에서 넘어온 이벤트를 처리하는 계약이다.
 */
public interface OrderEventUseCase {
    /** 재고 차감 완료 후 결제를 승인하고, 성공/실패 결과를 Outbound Port로 알린다. */
    void onStockDeducted(Long orderId, long amount);

    /** 결제가 끝난 주문이 취소되면 결제를 취소한다. */
    void onOrderCancelled(Long orderId, boolean wasPaid);
}

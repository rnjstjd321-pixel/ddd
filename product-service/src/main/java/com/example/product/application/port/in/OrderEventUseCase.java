package com.example.product.application.port.in;

import com.example.product.application.dto.OrderPlacedCommand;

/**
 * Inbound Port.
 * Order Context에서 넘어온 이벤트(주문 생성/취소)를 처리하는 계약이다.
 */
public interface OrderEventUseCase {
    /** 재고를 차감하고, 성공/실패 결과를 Outbound Port로 알린다. */
    void onOrderPlaced(OrderPlacedCommand command);

    /** 주문 취소에 따라 재고를 복구한다. */
    void onOrderCancelled(Long productId, int quantity);
}

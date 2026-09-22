package com.example.product.application.port.out;

import com.example.product.application.dto.OrderPlacedCommand;

/**
 * Outbound Port.
 * 재고 처리 결과를 외부(메시지 브로커)로 알릴 때 사용하는 계약이다.
 */
public interface StockEventPublisherPort {
    void publishStockDeducted(OrderPlacedCommand command);

    void publishStockDeductFailed(OrderPlacedCommand command, String reason);
}

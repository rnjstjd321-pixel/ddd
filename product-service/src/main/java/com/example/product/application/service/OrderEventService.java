package com.example.product.application.service;

import com.example.product.application.dto.OrderPlacedCommand;
import com.example.product.application.port.in.OrderEventUseCase;
import com.example.product.application.port.in.ProductUseCase;
import com.example.product.application.port.out.StockEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 주문 이벤트 처리 유스케이스.
 * 재고 차감 트랜잭션은 ProductUseCase가 소유하고, 이 서비스는 트랜잭션 밖에서 결과 이벤트만 발행한다.
 */
@Service
public class OrderEventService implements OrderEventUseCase {
    private static final Logger log = LoggerFactory.getLogger(OrderEventService.class);

    private final ProductUseCase productUseCase;
    private final StockEventPublisherPort stockEventPublisher;

    public OrderEventService(ProductUseCase productUseCase, StockEventPublisherPort stockEventPublisher) {
        this.productUseCase = productUseCase;
        this.stockEventPublisher = stockEventPublisher;
    }

    @Override
    public void onOrderPlaced(OrderPlacedCommand command) {
        try {
            productUseCase.decreaseProductStock(command.productId(), command.quantity());
        } catch (RuntimeException e) {
            log.error("Stock deduct failed orderId={}", command.orderId(), e);
            stockEventPublisher.publishStockDeductFailed(command, e.getMessage());
            return;
        }
        stockEventPublisher.publishStockDeducted(command);
    }

    @Override
    public void onOrderCancelled(Long productId, int quantity) {
        productUseCase.increaseProductStock(productId, quantity);
    }
}

package com.example.product.application.port.in;

import com.example.product.application.dto.CreateProductCommand;
import com.example.product.application.dto.ProductResponse;
import com.example.product.application.dto.StockReservationResult;

/**
 * Inbound Port.
 * 상품/재고 유스케이스 계약이다.
 */
public interface ProductUseCase {
    ProductResponse createProduct(CreateProductCommand command);

    ProductResponse getProduct(Long productId);

    ProductResponse decreaseProductStock(Long productId, int quantity);

    ProductResponse increaseProductStock(Long productId, int quantity);

    /** 주문 단위로 재고를 차감한다. 같은 주문에 대해 여러 번 호출돼도 한 번만 차감한다. */
    StockReservationResult reserveStock(Long orderId, Long productId, int quantity);

    /** 주문 단위로 재고를 복구한다(보상). 차감된 적이 없으면 이후 차감을 막는 표식만 남긴다. */
    void releaseStock(Long orderId, Long productId, int quantity);
}

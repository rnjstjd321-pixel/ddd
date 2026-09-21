package com.example.product.application.port.in;

import com.example.product.application.dto.CreateProductCommand;
import com.example.product.application.dto.ProductResponse;

/**
 * Inbound Port.
 * 웹 Inbound Adapter가 상품/재고 유스케이스를 호출할 때 사용하는 계약이다.
 */
public interface ProductUseCase {
    ProductResponse createProduct(CreateProductCommand command);

    ProductResponse getProduct(Long productId);

    ProductResponse decreaseProductStock(Long productId, int quantity);

    ProductResponse increaseProductStock(Long productId, int quantity);
}

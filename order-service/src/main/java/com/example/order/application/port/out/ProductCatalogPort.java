package com.example.order.application.port.out;

/**
 * Outbound Port.
 * 다른 Bounded Context(Product)의 상품 정보를 조회하는 계약이다.
 */
public interface ProductCatalogPort {
    ProductInfo getProduct(Long productId);

    record ProductInfo(Long id, String name, long price, int stock) {
    }
}

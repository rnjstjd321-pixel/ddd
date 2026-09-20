package com.example.order.application.port;

public interface ProductCatalogPort {
    ProductInfo getProduct(Long productId);

    record ProductInfo(Long id, String name, long price, int stock) {
    }
}

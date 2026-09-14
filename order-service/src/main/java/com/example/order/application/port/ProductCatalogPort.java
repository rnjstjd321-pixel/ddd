package com.example.order.application.port;

public interface ProductCatalogPort {
    ProductInfo getProduct(Long productId);

    void decreaseStock(Long productId, int quantity);

    void increaseStock(Long productId, int quantity);

    record ProductInfo(Long id, String name, long price, int stock) {
    }
}

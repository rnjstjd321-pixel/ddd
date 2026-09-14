package com.example.order.domain.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.util.Objects;

/**
 * 값 객체(Value Object).
 * 다른 Bounded Context(Product)의 상품을 주문 시점에 스냅샷으로만 보관한다.
 */
@Embeddable
public class ProductSnapshot {
    private Long productId;
    private String productName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price", precision = 19, scale = 2, nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "unit_currency", length = 3, nullable = false))
    })
    private Money unitPrice;

    protected ProductSnapshot() {
    }

    public ProductSnapshot(Long productId, String productName, Money unitPrice) {
        if (productId == null) {
            throw new IllegalArgumentException("productId is required");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("productName is required");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("unitPrice is required");
        }
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductSnapshot that)) {
            return false;
        }
        return Objects.equals(productId, that.productId)
                && Objects.equals(productName, that.productName)
                && Objects.equals(unitPrice, that.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, unitPrice);
    }
}

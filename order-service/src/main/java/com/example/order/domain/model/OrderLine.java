package com.example.order.domain.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Aggregate 내부 값 객체에 해당하는 주문 라인.
 * 외부에서는 Order(루트)를 통해서만 생성/변경한다.
 */
@Entity
@Table(name = "order_items")
public class OrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Embedded
    private ProductSnapshot productSnapshot;

    private int quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "line_amount", precision = 19, scale = 2, nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "line_currency", length = 3, nullable = false))
    })
    private Money lineAmount;

    protected OrderLine() {
    }

    static OrderLine of(Order order, ProductSnapshot productSnapshot, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        OrderLine line = new OrderLine();
        line.order = order;
        line.productSnapshot = productSnapshot;
        line.quantity = quantity;
        line.lineAmount = productSnapshot.getUnitPrice().multiply(quantity);
        return line;
    }

    public Long getProductId() {
        return productSnapshot.getProductId();
    }

    public String getProductName() {
        return productSnapshot.getProductName();
    }

    public Money getUnitPrice() {
        return productSnapshot.getUnitPrice();
    }

    public int getQuantity() {
        return quantity;
    }

    public Money subtotal() {
        return lineAmount;
    }
}

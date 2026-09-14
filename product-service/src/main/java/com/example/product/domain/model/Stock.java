package com.example.product.domain.model;

import com.example.product.domain.exception.InsufficientStockException;
import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * 값 객체(Value Object).
 * 재고 수량은 불변이며, 차감/증가는 새 Stock을 반환한다.
 */
@Embeddable
public class Stock {
    private int quantity;

    protected Stock() {
    }

    public Stock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("stock cannot be negative");
        }
        this.quantity = quantity;
    }

    public Stock decrease(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
        if (quantity < amount) {
            throw new InsufficientStockException();
        }
        return new Stock(quantity - amount);
    }

    public Stock increase(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
        return new Stock(quantity + amount);
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Stock stock)) {
            return false;
        }
        return quantity == stock.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity);
    }
}

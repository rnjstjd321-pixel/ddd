package com.example.product.domain.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 상품 Aggregate Root.
 * 가격/재고 변경은 이 루트를 통해서만 수행한다.
 */
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price", precision = 19, scale = 2, nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", length = 3, nullable = false))
    })
    private Money price;

    @Embedded
    @AttributeOverride(name = "quantity", column = @Column(name = "stock"))
    private Stock stock;

    protected Product() {
    }

    public static Product create(String name, long price, int stock) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        Product product = new Product();
        product.name = name;
        product.price = Money.krw(price);
        product.stock = new Stock(stock);
        return product;
    }

    public void decreaseStock(int quantity) {
        this.stock = this.stock.decrease(quantity);
    }

    public void increaseStock(int quantity) {
        this.stock = this.stock.increase(quantity);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPrice() {
        return price.toLong();
    }

    public int getStock() {
        return stock.getQuantity();
    }
}

package com.example.product.domain.model;

import com.example.product.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    @Test
    void createProduct() {
        Product product = Product.create("MacBook Pro", 2500000, 10);

        assertEquals("MacBook Pro", product.getName());
        assertEquals(2500000, product.getPrice());
        assertEquals(10, product.getStock());
    }

    @Test
    void decreaseStock() {
        Product product = Product.create("MacBook Pro", 2500000, 10);

        product.decreaseStock(3);

        assertEquals(7, product.getStock());
    }

    @Test
    void cannotDecreaseMoreThanStock() {
        Product product = Product.create("MacBook Pro", 2500000, 2);

        assertThrows(InsufficientStockException.class, () -> product.decreaseStock(3));
    }

    @Test
    void increaseStock() {
        Product product = Product.create("MacBook Pro", 2500000, 10);

        product.increaseStock(2);

        assertEquals(12, product.getStock());
    }
}

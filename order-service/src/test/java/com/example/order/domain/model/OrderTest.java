package com.example.order.domain.model;

import com.example.order.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    void createAndAddLine() {
        Order order = Order.create();
        order.addLine(new ProductSnapshot(1L, "MacBook Pro", Money.krw(2500000)), 2);

        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(5000000, order.totalAmount().toLong());
        assertEquals(1L, order.firstLine().getProductId());
        assertEquals("MacBook Pro", order.firstLine().getProductName());
        assertEquals(2, order.firstLine().getQuantity());
    }

    @Test
    void markPaidFromCreated() {
        Order order = placedOrder(1);

        order.markPaid();

        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void cannotPayAlreadyPaidOrder() {
        Order order = placedOrder(1);
        order.markPaid();

        assertThrows(InvalidOrderStateException.class, order::markPaid);
    }

    @Test
    void cannotAddLineAfterPaid() {
        Order order = placedOrder(1);
        order.markPaid();

        assertThrows(InvalidOrderStateException.class,
                () -> order.addLine(new ProductSnapshot(1L, "MacBook Pro", Money.krw(2500000)), 1));
    }

    @Test
    void cancelPaidOrder() {
        Order order = placedOrder(1);
        order.markPaid();

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void cannotCancelAlreadyCancelledOrder() {
        Order order = placedOrder(1);
        order.cancel();

        assertThrows(InvalidOrderStateException.class, order::cancel);
    }

    @Test
    void rejectNonPositiveQuantity() {
        Order order = Order.create();
        ProductSnapshot snapshot = new ProductSnapshot(1L, "MacBook Pro", Money.krw(2500000));

        assertThrows(IllegalArgumentException.class, () -> order.addLine(snapshot, 0));
    }

    private Order placedOrder(int quantity) {
        Order order = Order.create();
        order.addLine(new ProductSnapshot(1L, "MacBook Pro", Money.krw(2500000)), quantity);
        return order;
    }
}

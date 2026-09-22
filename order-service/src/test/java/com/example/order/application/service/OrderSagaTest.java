package com.example.order.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.order.application.dto.CreateOrderCommand;
import com.example.order.application.dto.OrderResponse;
import com.example.order.application.port.out.OrderEventPublisherPort;
import com.example.order.application.port.out.OrderRepositoryPort;
import com.example.order.application.port.out.ProductCatalogPort;
import com.example.order.domain.event.OrderCancelled;
import com.example.order.domain.event.OrderPlaced;
import com.example.order.domain.model.Order;
import com.example.order.domain.model.OrderStatus;
import com.example.order.domain.service.PlaceOrderService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Saga choreography에서 Order가 결제/재고 결과 이벤트에 어떻게 반응하고 보상을 요청하는지 검증한다.
 */
class OrderSagaTest {
    private final Map<Long, Order> orders = new HashMap<>();
    private final List<String> events = new ArrayList<>();
    private long sequence = 0;

    private OrderApplicationService service;

    @BeforeEach
    void setUp() {
        OrderRepositoryPort repository = new OrderRepositoryPort() {
            public Order save(Order order) {
                if (order.getId() == null) {
                    setId(order, ++sequence);
                }
                orders.put(order.getId(), order);
                return order;
            }

            public Optional<Order> findById(Long id) {
                return Optional.ofNullable(orders.get(id));
            }
        };
        OrderEventPublisherPort publisher = new OrderEventPublisherPort() {
            public void publish(OrderPlaced event) {
                events.add("placed:" + event.orderId());
            }

            public void publish(OrderCancelled event) {
                events.add("cancelled:" + event.orderId() + ":wasPaid=" + event.wasPaid());
            }
        };
        ProductCatalogPort catalog = productId -> new ProductCatalogPort.ProductInfo(productId, "MacBook", 1_000, 10);
        service = new OrderApplicationService(repository, new PlaceOrderService(), catalog, publisher);
    }

    @Test
    void paymentApprovedMarksOrderPaid() {
        OrderResponse order = service.createOrder(new CreateOrderCommand(1L, 2));

        service.markOrderPaid(order.id());

        assertEquals(OrderStatus.PAID, orders.get(order.id()).getStatus());
        assertEquals(List.of("placed:1"), events);
    }

    @Test
    void paymentFailureCancelsOrderAndRequestsStockRestore() {
        OrderResponse order = service.createOrder(new CreateOrderCommand(1L, 2));

        service.failOrderAfterPaymentFailure(order.id());
        service.failOrderAfterPaymentFailure(order.id());

        assertEquals(OrderStatus.CANCELLED, orders.get(order.id()).getStatus());
        assertEquals(List.of("placed:1", "cancelled:1:wasPaid=false"), events);
    }

    @Test
    void stockFailureCancelsOrderWithoutCompensationEvent() {
        OrderResponse order = service.createOrder(new CreateOrderCommand(1L, 2));

        service.failOrderAfterStockFailure(order.id());

        assertEquals(OrderStatus.CANCELLED, orders.get(order.id()).getStatus());
        assertEquals(List.of("placed:1"), events);
    }

    @Test
    void latePaymentApprovedOnCancelledOrderRequestsPaymentCancel() {
        OrderResponse order = service.createOrder(new CreateOrderCommand(1L, 2));
        service.cancelOrder(order.id());

        service.markOrderPaid(order.id());

        assertEquals(OrderStatus.CANCELLED, orders.get(order.id()).getStatus());
        assertEquals(List.of("placed:1", "cancelled:1:wasPaid=false", "cancelled:1:wasPaid=true"), events);
    }

    private static void setId(Order order, long id) {
        try {
            Field field = Order.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(order, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}

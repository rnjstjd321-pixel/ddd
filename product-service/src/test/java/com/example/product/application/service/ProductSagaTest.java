package com.example.product.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.product.application.dto.OrderPlacedCommand;
import com.example.product.application.dto.StockReservationResult;
import com.example.product.application.port.out.ProductRepositoryPort;
import com.example.product.application.port.out.StockEventPublisherPort;
import com.example.product.application.port.out.StockReservationRepositoryPort;
import com.example.product.domain.exception.InsufficientStockException;
import com.example.product.domain.model.Product;
import com.example.product.domain.model.StockReservation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Saga choreography에서 Product가 중복/역순 이벤트에도 재고를 한 번만 차감/복구하는지 검증한다.
 */
class ProductSagaTest {
    private final Map<Long, Product> products = new HashMap<>();
    private final Map<Long, StockReservation> reservations = new HashMap<>();
    private final List<String> published = new ArrayList<>();

    private ProductApplicationService productService;
    private OrderEventService orderEventService;

    @BeforeEach
    void setUp() throws Exception {
        Product product = Product.create("MacBook", 2_500_000, 10);
        Field id = Product.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(product, 1L);
        products.put(1L, product);

        ProductRepositoryPort productRepository = new ProductRepositoryPort() {
            public Product save(Product p) {
                return p;
            }

            public Optional<Product> findById(Long productId) {
                return Optional.ofNullable(products.get(productId));
            }

            public Optional<Product> findByIdForUpdate(Long productId) {
                return findById(productId);
            }
        };
        StockReservationRepositoryPort reservationRepository = new StockReservationRepositoryPort() {
            public StockReservation save(StockReservation r) {
                reservations.put(r.getOrderId(), r);
                return r;
            }

            public Optional<StockReservation> findByOrderId(Long orderId) {
                return Optional.ofNullable(reservations.get(orderId));
            }
        };
        StockEventPublisherPort publisher = new StockEventPublisherPort() {
            public void publishStockDeducted(OrderPlacedCommand command) {
                published.add("deducted:" + command.orderId());
            }

            public void publishStockDeductFailed(OrderPlacedCommand command, String reason) {
                published.add("failed:" + command.orderId());
            }
        };
        productService = new ProductApplicationService(productRepository, reservationRepository);
        orderEventService = new OrderEventService(productService, publisher);
    }

    @Test
    void duplicateOrderPlacedDeductsStockOnce() {
        OrderPlacedCommand command = new OrderPlacedCommand(100L, 1L, 3, 7_500_000, "KRW");

        orderEventService.onOrderPlaced(command);
        orderEventService.onOrderPlaced(command);

        assertEquals(7, products.get(1L).getStock());
        assertEquals(List.of("deducted:100", "deducted:100"), published);
    }

    @Test
    void duplicateCancelRestoresStockOnce() {
        orderEventService.onOrderPlaced(new OrderPlacedCommand(100L, 1L, 3, 7_500_000, "KRW"));

        orderEventService.onOrderCancelled(100L, 1L, 3);
        orderEventService.onOrderCancelled(100L, 1L, 3);

        assertEquals(10, products.get(1L).getStock());
    }

    @Test
    void cancelBeforeDeductBlocksLaterDeduct() {
        orderEventService.onOrderCancelled(100L, 1L, 3);
        orderEventService.onOrderPlaced(new OrderPlacedCommand(100L, 1L, 3, 7_500_000, "KRW"));

        assertEquals(10, products.get(1L).getStock());
        assertEquals(List.of(), published);
    }

    @Test
    void insufficientStockPublishesFailureAndKeepsStock() {
        orderEventService.onOrderPlaced(new OrderPlacedCommand(100L, 1L, 11, 27_500_000, "KRW"));

        assertEquals(10, products.get(1L).getStock());
        assertEquals(List.of("failed:100"), published);
        assertEquals(0, reservations.size());
    }

    @Test
    void reserveThrowsWhenStockIsInsufficient() {
        assertThrows(InsufficientStockException.class, () -> productService.reserveStock(100L, 1L, 11));
        assertEquals(StockReservationResult.RESERVED, productService.reserveStock(100L, 1L, 10));
        assertEquals(0, products.get(1L).getStock());
    }
}

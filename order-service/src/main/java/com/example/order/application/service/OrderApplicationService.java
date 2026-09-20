package com.example.order.application.service;

import com.example.order.application.dto.CreateOrderCommand;
import com.example.order.application.dto.OrderResponse;
import com.example.order.application.port.ProductCatalogPort;
import com.example.order.domain.event.DomainEventPublisher;
import com.example.order.domain.exception.OrderNotFoundException;
import com.example.order.domain.model.Money;
import com.example.order.domain.model.Order;
import com.example.order.domain.model.OrderStatus;
import com.example.order.domain.model.ProductSnapshot;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.domain.service.PlaceOrderService;
import com.example.order.domain.service.PlaceOrderService.OrderLineRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 서비스.
 * 상품 조회만 동기 REST로 하고, 재고/결제는 Kafka 이벤트로 비동기 처리한다.
 */
@Service
@Transactional
public class OrderApplicationService {
    private final OrderRepository orderRepository;
    private final PlaceOrderService placeOrderService;
    private final ProductCatalogPort productCatalogPort;
    private final DomainEventPublisher domainEventPublisher;

    public OrderApplicationService(
            OrderRepository orderRepository,
            PlaceOrderService placeOrderService,
            ProductCatalogPort productCatalogPort,
            DomainEventPublisher domainEventPublisher) {
        this.orderRepository = orderRepository;
        this.placeOrderService = placeOrderService;
        this.productCatalogPort = productCatalogPort;
        this.domainEventPublisher = domainEventPublisher;
    }

    public OrderResponse createOrder(CreateOrderCommand command) {
        ProductCatalogPort.ProductInfo product = productCatalogPort.getProduct(command.productId());

        Order order = placeOrderService.place(List.of(
                new OrderLineRequest(
                        new ProductSnapshot(product.id(), product.name(), Money.krw(product.price())),
                        command.quantity()
                )
        ));

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return OrderResponse.from(findOrder(orderId));
    }

    public OrderResponse cancelOrder(Long orderId) {
        Order order = findOrder(orderId);
        boolean wasPaid = order.isPaid();
        order.cancel();
        domainEventPublisher.publish(order.cancelledEvent(wasPaid));
        return OrderResponse.from(orderRepository.save(order));
    }

    public void markOrderPaid(Long orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() != OrderStatus.CREATED) {
            return;
        }
        order.markPaid();
        orderRepository.save(order);
    }

    public void failOrderAfterStockFailure(Long orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }
        order.cancel();
        orderRepository.save(order);
    }

    public void failOrderAfterPaymentFailure(Long orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }
        order.cancel();
        domainEventPublisher.publish(order.cancelledEvent(false));
        orderRepository.save(order);
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}

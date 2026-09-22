package com.example.order.application.service;

import com.example.order.application.dto.CreateOrderCommand;
import com.example.order.application.dto.OrderResponse;
import com.example.order.application.port.out.ProductCatalogPort;
import com.example.order.application.port.in.OrderUseCase;
import com.example.order.application.port.out.OrderEventPublisherPort;
import com.example.order.domain.exception.OrderNotFoundException;
import com.example.order.domain.model.Money;
import com.example.order.domain.model.Order;
import com.example.order.domain.model.OrderStatus;
import com.example.order.domain.model.ProductSnapshot;
import com.example.order.application.port.out.OrderRepositoryPort;
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
public class OrderApplicationService implements OrderUseCase {
    private final OrderRepositoryPort orderRepository;
    private final PlaceOrderService placeOrderService;
    private final ProductCatalogPort productCatalogPort;
    private final OrderEventPublisherPort domainEventPublisher;

    public OrderApplicationService(
            OrderRepositoryPort orderRepository,
            PlaceOrderService placeOrderService,
            ProductCatalogPort productCatalogPort,
            OrderEventPublisherPort domainEventPublisher) {
        this.orderRepository = orderRepository;
        this.placeOrderService = placeOrderService;
        this.productCatalogPort = productCatalogPort;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public OrderResponse createOrder(CreateOrderCommand command) {
        ProductCatalogPort.ProductInfo product = productCatalogPort.getProduct(command.productId());

        Order placed = placeOrderService.place(List.of(
                new OrderLineRequest(
                        new ProductSnapshot(product.id(), product.name(), Money.krw(product.price())),
                        command.quantity()
                )
        ));

        Order saved = orderRepository.save(placed);
        domainEventPublisher.publish(saved.placedEvent());
        return OrderResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return OrderResponse.from(findOrder(orderId));
    }

    @Override
    public OrderResponse cancelOrder(Long orderId) {
        Order order = findOrder(orderId);
        boolean wasPaid = order.isPaid();
        order.cancel();
        domainEventPublisher.publish(order.cancelledEvent(wasPaid));
        return OrderResponse.from(orderRepository.save(order));
    }

    @Override
    public void markOrderPaid(Long orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            // 취소된 주문에 결제 승인이 뒤늦게 도착: 결제 취소(보상)를 다시 요청한다. 받는 쪽은 멱등하다.
            domainEventPublisher.publish(order.cancelledEvent(true));
            return;
        }
        if (order.getStatus() != OrderStatus.CREATED) {
            return;
        }
        order.markPaid();
        orderRepository.save(order);
    }

    @Override
    public void failOrderAfterStockFailure(Long orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }
        order.cancel();
        orderRepository.save(order);
    }

    @Override
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

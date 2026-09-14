package com.example.order.application.service;

import com.example.order.application.dto.CreateOrderCommand;
import com.example.order.application.dto.OrderResponse;
import com.example.order.application.exception.PaymentApprovalFailedException;
import com.example.order.application.port.PaymentGatewayPort;
import com.example.order.application.port.ProductCatalogPort;
import com.example.order.domain.exception.OrderNotFoundException;
import com.example.order.domain.model.Money;
import com.example.order.domain.model.Order;
import com.example.order.domain.model.ProductSnapshot;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.domain.service.PlaceOrderService;
import com.example.order.domain.service.PlaceOrderService.OrderLineRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 서비스.
 * 상품/결제 같은 다른 컨텍스트 연동만 조율하고,
 * 주문 생성 규칙과 이벤트 발행은 PlaceOrderService / Order Aggregate에 맡긴다.
 */
@Service
@Transactional
public class OrderApplicationService {
    private final OrderRepository orderRepository;
    private final PlaceOrderService placeOrderService;
    private final ProductCatalogPort productCatalogPort;
    private final PaymentGatewayPort paymentGatewayPort;

    public OrderApplicationService(
            OrderRepository orderRepository,
            PlaceOrderService placeOrderService,
            ProductCatalogPort productCatalogPort,
            PaymentGatewayPort paymentGatewayPort) {
        this.orderRepository = orderRepository;
        this.placeOrderService = placeOrderService;
        this.productCatalogPort = productCatalogPort;
        this.paymentGatewayPort = paymentGatewayPort;
    }

    public OrderResponse createOrder(CreateOrderCommand command) {
        ProductCatalogPort.ProductInfo product = productCatalogPort.getProduct(command.productId());
        productCatalogPort.decreaseStock(command.productId(), command.quantity());

        // 도메인 서비스가 Aggregate를 만들고 OrderPlaced 이벤트를 발행한다.
        Order order = placeOrderService.place(List.of(
                new OrderLineRequest(
                        new ProductSnapshot(product.id(), product.name(), Money.krw(product.price())),
                        command.quantity()
                )
        ));

        PaymentGatewayPort.PaymentResult payment = paymentGatewayPort.approve(
                order.getId(),
                order.totalAmount().toLong()
        );
        if (!payment.isPaid()) {
            throw new PaymentApprovalFailedException();
        }

        order.markPaid();
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return OrderResponse.from(findOrder(orderId));
    }

    public OrderResponse cancelOrder(Long orderId) {
        Order order = findOrder(orderId);
        boolean wasPaid = order.isPaid();
        Long productId = order.firstLine().getProductId();
        int quantity = order.firstLine().getQuantity();

        order.cancel();

        productCatalogPort.increaseStock(productId, quantity);
        if (wasPaid) {
            paymentGatewayPort.cancelByOrderId(orderId);
        }

        return OrderResponse.from(orderRepository.save(order));
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}

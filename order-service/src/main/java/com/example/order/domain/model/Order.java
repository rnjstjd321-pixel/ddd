package com.example.order.domain.model;

import com.example.order.domain.event.OrderPlaced;
import com.example.order.domain.exception.InvalidOrderStateException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 주문 Aggregate Root.
 * 주문 라인 추가, 금액 계산, 상태 전이는 이 루트를 통해서만 수행한다.
 */
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderLine> orderLines = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount", precision = 19, scale = 2, nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", length = 3, nullable = false))
    })
    private Money totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    protected Order() {
    }

    public static Order create() {
        Order order = new Order();
        order.status = OrderStatus.CREATED;
        order.totalAmount = Money.zero(Money.KRW);
        return order;
    }

    public void addLine(ProductSnapshot snapshot, int quantity) {
        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderStateException("확정된 주문에는 상품을 추가할 수 없습니다.");
        }
        orderLines.add(OrderLine.of(this, snapshot, quantity));
        this.totalAmount = calculateTotal();
    }

    public void markPaid() {
        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderStateException("이미 결제된 주문입니다.");
        }
        status = OrderStatus.PAID;
    }

    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Order is already cancelled");
        }
        status = OrderStatus.CANCELLED;
    }

    public boolean isPaid() {
        return status == OrderStatus.PAID;
    }

    public Money totalAmount() {
        return totalAmount;
    }

    public OrderPlaced placedEvent() {
        return new OrderPlaced(id, totalAmount);
    }

    public Long getId() {
        return id;
    }

    public List<OrderLine> getOrderLines() {
        return Collections.unmodifiableList(orderLines);
    }

    public OrderLine firstLine() {
        return orderLines.get(0);
    }

    public OrderStatus getStatus() {
        return status;
    }

    private Money calculateTotal() {
        return orderLines.stream()
                .map(OrderLine::subtotal)
                .reduce(Money.zero(Money.KRW), Money::add);
    }
}

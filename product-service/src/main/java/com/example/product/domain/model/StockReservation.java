package com.example.product.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 주문 단위 재고 차감 기록.
 * orderId가 PK이므로 같은 주문의 이벤트가 중복/역순으로 도착해도 재고가 한 번만 차감/복구된다.
 * RELEASED 상태는 "차감 전에 취소가 먼저 도착한 경우"의 표식(tombstone)으로도 쓰인다.
 */
@Entity
@Table(name = "stock_reservations")
public class StockReservation {
    @Id
    @Column(name = "order_id")
    private Long orderId;

    private Long productId;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    protected StockReservation() {
    }

    public static StockReservation deducted(Long orderId, Long productId, int quantity) {
        return of(orderId, productId, quantity, ReservationStatus.DEDUCTED);
    }

    /** 재고를 차감하기 전에 주문 취소가 먼저 도착했을 때 남기는 표식. 이후 차감 요청은 무시된다. */
    public static StockReservation cancelledBeforeDeduct(Long orderId, Long productId, int quantity) {
        return of(orderId, productId, quantity, ReservationStatus.RELEASED);
    }

    private static StockReservation of(Long orderId, Long productId, int quantity, ReservationStatus status) {
        if (orderId == null || productId == null) {
            throw new IllegalArgumentException("orderId and productId are required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
        StockReservation reservation = new StockReservation();
        reservation.orderId = orderId;
        reservation.productId = productId;
        reservation.quantity = quantity;
        reservation.status = status;
        return reservation;
    }

    public void release() {
        if (status != ReservationStatus.DEDUCTED) {
            throw new IllegalStateException("Only DEDUCTED reservations can be released");
        }
        status = ReservationStatus.RELEASED;
    }

    public boolean isDeducted() {
        return status == ReservationStatus.DEDUCTED;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}

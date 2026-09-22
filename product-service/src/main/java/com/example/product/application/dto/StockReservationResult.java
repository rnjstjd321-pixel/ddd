package com.example.product.application.dto;

public enum StockReservationResult {
    /** 이번 요청에서 재고를 차감했다. */
    RESERVED,
    /** 이미 차감된 주문이다(중복 이벤트). */
    ALREADY_RESERVED,
    /** 이미 취소/복구된 주문이다. 재고를 차감하지 않는다. */
    ALREADY_RELEASED
}

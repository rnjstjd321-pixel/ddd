package com.example.product.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * 값 객체(Value Object).
 * 상품 가격은 식별자가 아니라 금액+통화 값으로 다룬다.
 */
@Embeddable
public class Money {
    public static final String KRW = "KRW";

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(length = 3, nullable = false)
    private String currency;

    protected Money() {
    }

    private Money(BigDecimal amount, String currency) {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("금액과 통화는 null일 수 없습니다.");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("금액은 음수일 수 없습니다.");
        }
        this.amount = amount;
        this.currency = currency;
    }

    public static Money of(long amount, String currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public static Money krw(long amount) {
        return of(amount, KRW);
    }

    public long toLong() {
        return amount.longValueExact();
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money money)) {
            return false;
        }
        return amount.compareTo(money.amount) == 0 && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }
}

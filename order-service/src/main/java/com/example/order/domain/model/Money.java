package com.example.order.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * 값 객체(Value Object).
 * 식별자 없이 금액+통화로 동일성을 판단하고, 생성 시점에 유효성을 강제한다.
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

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money krw(long amount) {
        return of(amount, KRW);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("같은 통화 단위만 더할 수 있습니다.");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public long toLong() {
        return amount.longValueExact();
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

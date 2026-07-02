package com.school.billing.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount) {
    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Payment amount is required.");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Payment amount cannot be negative.");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }
}

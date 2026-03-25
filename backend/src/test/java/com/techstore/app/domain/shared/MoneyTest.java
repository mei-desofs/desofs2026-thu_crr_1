package com.techstore.app.domain.shared;

import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithValue() {
        BigDecimal amount = new BigDecimal("99.99");

        Money money = new Money(amount);

        assertEquals(amount, money.getValue());
    }

    @Test
    void shouldAllowUpdatingValue() {
        Money money = new Money(new BigDecimal("10.00"));
        BigDecimal amount = new BigDecimal("25.50");

        money.setValue(amount);

        assertEquals(amount, money.getValue());
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        Money money = new Money(new BigDecimal("10.00"));

        assertThrows(BusinessException.class, () -> money.setValue(null));
    }

    @Test
    void shouldThrowExceptionWhenCreatingMoneyWithNullValue() {
        assertThrows(BusinessException.class, () -> new Money(null));
    }

    @Test
    void shouldThrowExceptionWhenCreatingMoneyWithZero() {
        assertThrows(BusinessException.class, () -> new Money(BigDecimal.ZERO));
    }

    @Test
    void shouldThrowExceptionWhenCreatingMoneyWithNegativeValue() {
        assertThrows(BusinessException.class, () -> new Money(new BigDecimal("-0.01")));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingMoneyWithZero() {
        Money money = new Money(new BigDecimal("10.00"));

        assertThrows(BusinessException.class, () -> money.setValue(BigDecimal.ZERO));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingMoneyWithNegativeValue() {
        Money money = new Money(new BigDecimal("10.00"));

        assertThrows(BusinessException.class, () -> money.setValue(new BigDecimal("-0.01")));
    }
}

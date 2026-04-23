package com.techstore.app.domain.shared;

import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class MoneyTest {

    @Test
    void shouldCreateMoneyWithValue() {
        BigDecimal amount = new BigDecimal("99.99");

        Money money = new Money(amount);

        assertEquals(amount, money.getValue());
    }

    @Test
    void shouldCreateNewInstanceWhenValueChanges() {
        Money original = new Money(new BigDecimal("10.00"));
        BigDecimal newAmount = new BigDecimal("25.50");

        Money updated = new Money(newAmount);

        assertNotSame(original, updated);
        assertEquals(newAmount, updated.getValue());
        assertEquals(new BigDecimal("10.00"), original.getValue());
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
}
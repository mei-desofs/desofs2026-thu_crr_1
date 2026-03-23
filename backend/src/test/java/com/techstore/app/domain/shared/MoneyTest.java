package com.techstore.app.domain.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithValue() {
        BigDecimal amount = new BigDecimal("99.99");

        Money money = new Money(amount);

        assertEquals(amount, money.getValue());
    }

    @Test
    void shouldAllowUpdatingValue() {
        Money money = new Money();
        BigDecimal amount = new BigDecimal("25.50");

        money.setValue(amount);

        assertEquals(amount, money.getValue());
    }

    @Test
    void shouldCreateMoneyWithDefaultConstructor() {
        Money money = new Money();

        assertNull(money.getValue());
    }
}

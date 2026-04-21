package com.techstore.app.domain.shared;

import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QuantityTest {

    @Test
    void shouldCreateQuantityWithValidValue() {
        Quantity quantity = new Quantity(5);

        assertEquals(5, quantity.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        assertThrows(BusinessException.class, () -> new Quantity(null));
    }

    @Test
    void shouldThrowExceptionWhenValueIsZero() {
        assertThrows(BusinessException.class, () -> new Quantity(0));
    }

    @Test
    void shouldThrowExceptionWhenValueIsNegative() {
        assertThrows(BusinessException.class, () -> new Quantity(-1));
    }
}
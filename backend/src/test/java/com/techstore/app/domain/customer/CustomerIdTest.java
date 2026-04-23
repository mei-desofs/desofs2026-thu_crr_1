package com.techstore.app.domain.customer;

import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerIdTest {

    @Test
    void shouldCreateCustomerIdWithUuid() {
        UUID uuid = UUID.randomUUID();

        CustomerId customerId = new CustomerId(uuid);

        assertEquals(uuid, customerId.getId());
    }

    @Test
    void shouldThrowExceptionWhenUuidIsNull() {
        BusinessException exception = assertThrows(BusinessException.class, () -> new CustomerId(null));

        assertEquals("Customer ID cannot be null.", exception.getMessage());
    }

    @Test
    void shouldCreateNewRandomCustomerId() {
        CustomerId customerId = CustomerId.newId();

        assertNotNull(customerId);
        assertNotNull(customerId.getId());
    }

    @Test
    void shouldCreateCustomerIdFromString() {
        UUID uuid = UUID.randomUUID();

        CustomerId customerId = CustomerId.fromString(uuid.toString());

        assertEquals(uuid, customerId.getId());
    }

    @Test
    void shouldThrowExceptionWhenStringIsInvalidUuid() {
        assertThrows(IllegalArgumentException.class, () -> CustomerId.fromString("invalid-uuid"));
    }
}

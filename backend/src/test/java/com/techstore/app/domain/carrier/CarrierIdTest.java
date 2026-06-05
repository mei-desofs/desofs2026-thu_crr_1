package com.techstore.app.domain.carrier;

import com.techstore.app.domain.customer.CustomerId;
import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CarrierIdTest {
    @Test
    void shouldCreateCarrierIdWithUuid() {
        UUID uuid = UUID.randomUUID();

        CarrierId carrierId = new CarrierId(uuid);

        assertEquals(uuid, carrierId.getId());
    }

    @Test
    void shouldThrowExceptionWhenUuidIsNull() {
        BusinessException exception = assertThrows(BusinessException.class, () -> new CarrierId(null));

        assertEquals("Carrier ID cannot be null.", exception.getMessage());
    }

    @Test
    void shouldCreateNewRandomCarrierId() {
        CarrierId carrierId = CarrierId.newId();

        assertNotNull(carrierId);
        assertNotNull(carrierId.getId());
    }

    @Test
    void shouldCreateCarrierIdFromString() {
        UUID uuid = UUID.randomUUID();

        CarrierId carrierId = CarrierId.fromString(uuid.toString());

        assertEquals(uuid, carrierId.getId());
    }

    @Test
    void shouldThrowExceptionWhenStringIsInvalidUuid() {
        assertThrows(IllegalArgumentException.class, () -> CarrierId.fromString("invalid-uuid"));
    }
}

package com.techstore.app.domain.order;

import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderIdTest {

    @Test
    void shouldCreateOrderIdWithUuid() {
        UUID uuid = UUID.randomUUID();

        OrderId orderId = new OrderId(uuid);

        assertEquals(uuid, orderId.getId());
    }

    @Test
    void shouldThrowExceptionWhenUuidIsNull() {
        BusinessException exception = assertThrows(BusinessException.class, () -> new OrderId(null));

        assertEquals("Order ID cannot be null.", exception.getMessage());
    }

    @Test
    void shouldCreateNewRandomOrderId() {
        OrderId orderId = OrderId.newId();

        assertNotNull(orderId);
        assertNotNull(orderId.getId());
    }

    @Test
    void shouldCreateOrderIdFromString() {
        UUID uuid = UUID.randomUUID();

        OrderId orderId = OrderId.fromString(uuid.toString());

        assertEquals(uuid, orderId.getId());
    }

    @Test
    void shouldThrowExceptionWhenStringIsInvalidUuid() {
        assertThrows(IllegalArgumentException.class, () -> OrderId.fromString("invalid-uuid"));
    }

    @Test
    void shouldConsiderTwoOrderIdsEqualWhenHavingSameUuid() {
        UUID uuid = UUID.randomUUID();
        OrderId orderId1 = new OrderId(uuid);
        OrderId orderId2 = new OrderId(uuid);

        assertEquals(orderId1, orderId2);
    }

    @Test
    void shouldConsiderTwoOrderIdsNotEqualWhenHavingDifferentUuids() {
        OrderId orderId1 = OrderId.newId();
        OrderId orderId2 = OrderId.newId();

        assertNotEquals(orderId1, orderId2);
    }
}

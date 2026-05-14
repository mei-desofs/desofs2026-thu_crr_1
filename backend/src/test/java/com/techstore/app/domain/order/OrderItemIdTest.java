package com.techstore.app.domain.order;

import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemIdTest {

    @Test
    void shouldCreateOrderItemIdWithUuid() {
        UUID uuid = UUID.randomUUID();

        OrderItemId orderItemId = new OrderItemId(uuid);

        assertEquals(uuid, orderItemId.getId());
    }

    @Test
    void shouldThrowExceptionWhenUuidIsNull() {
        BusinessException exception = assertThrows(BusinessException.class, () -> new OrderItemId(null));

        assertEquals("Order Item ID cannot be null.", exception.getMessage());
    }

    @Test
    void shouldCreateNewRandomOrderItemId() {
        OrderItemId orderItemId = OrderItemId.newId();

        assertNotNull(orderItemId);
        assertNotNull(orderItemId.getId());
    }

    @Test
    void shouldCreateOrderItemIdFromString() {
        UUID uuid = UUID.randomUUID();

        OrderItemId orderItemId = OrderItemId.fromString(uuid.toString());

        assertEquals(uuid, orderItemId.getId());
    }

    @Test
    void shouldThrowExceptionWhenStringIsInvalidUuid() {
        assertThrows(IllegalArgumentException.class, () -> OrderItemId.fromString("invalid-uuid"));
    }

    @Test
    void shouldConsiderTwoOrderItemIdsEqualWhenHavingSameUuid() {
        UUID uuid = UUID.randomUUID();
        OrderItemId orderItemId1 = new OrderItemId(uuid);
        OrderItemId orderItemId2 = new OrderItemId(uuid);

        assertEquals(orderItemId1, orderItemId2);
    }

    @Test
    void shouldConsiderTwoOrderItemIdsNotEqualWhenHavingDifferentUuids() {
        OrderItemId orderItemId1 = OrderItemId.newId();
        OrderItemId orderItemId2 = OrderItemId.newId();

        assertNotEquals(orderItemId1, orderItemId2);
    }
}

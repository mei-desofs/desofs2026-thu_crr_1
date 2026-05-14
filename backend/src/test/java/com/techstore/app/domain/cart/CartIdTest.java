package com.techstore.app.domain.cart;

import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CartIdTest {

    @Test
    void shouldCreateCartIdWithUuid() {
        UUID uuid = UUID.randomUUID();

        CartId cartId = new CartId(uuid);

        assertEquals(uuid, cartId.getId());
    }

    @Test
    void shouldThrowExceptionWhenUuidIsNull() {
        BusinessException exception = assertThrows(BusinessException.class, () -> new CartId(null));

        assertEquals("Cart ID cannot be null.", exception.getMessage());
    }

    @Test
    void shouldCreateNewRandomCartId() {
        CartId cartId = CartId.newId();

        assertNotNull(cartId);
        assertNotNull(cartId.getId());
    }

    @Test
    void shouldCreateCartIdFromString() {
        UUID uuid = UUID.randomUUID();

        CartId cartId = CartId.fromString(uuid.toString());

        assertEquals(uuid, cartId.getId());
    }

    @Test
    void shouldThrowExceptionWhenStringIsInvalidUuid() {
        assertThrows(IllegalArgumentException.class, () -> CartId.fromString("invalid-uuid"));
    }

    @Test
    void shouldConsiderTwoCartIdsEqualWhenHavingSameUuid() {
        UUID uuid = UUID.randomUUID();
        CartId cartId1 = new CartId(uuid);
        CartId cartId2 = new CartId(uuid);

        assertEquals(cartId1, cartId2);
    }

    @Test
    void shouldConsiderTwoCartIdsNotEqualWhenHavingDifferentUuids() {
        CartId cartId1 = CartId.newId();
        CartId cartId2 = CartId.newId();

        assertNotEquals(cartId1, cartId2);
    }
}

package com.techstore.app.domain.cart;

import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CartItemIdTest {

    @Test
    void shouldCreateCartItemIdWithUuid() {
        UUID uuid = UUID.randomUUID();

        CartItemId cartItemId = new CartItemId(uuid);

        assertEquals(uuid, cartItemId.getId());
    }

    @Test
    void shouldThrowExceptionWhenUuidIsNull() {
        BusinessException exception = assertThrows(BusinessException.class, () -> new CartItemId(null));

        assertEquals("Cart Item ID cannot be null.", exception.getMessage());
    }

    @Test
    void shouldCreateNewRandomCartItemId() {
        CartItemId cartItemId = CartItemId.newId();

        assertNotNull(cartItemId);
        assertNotNull(cartItemId.getId());
    }

    @Test
    void shouldCreateCartItemIdFromString() {
        UUID uuid = UUID.randomUUID();

        CartItemId cartItemId = CartItemId.fromString(uuid.toString());

        assertEquals(uuid, cartItemId.getId());
    }

    @Test
    void shouldThrowExceptionWhenStringIsInvalidUuid() {
        assertThrows(IllegalArgumentException.class, () -> CartItemId.fromString("invalid-uuid"));
    }

    @Test
    void shouldConsiderTwoCartItemIdsEqualWhenHavingSameUuid() {
        UUID uuid = UUID.randomUUID();
        CartItemId cartItemId1 = new CartItemId(uuid);
        CartItemId cartItemId2 = new CartItemId(uuid);

        assertEquals(cartItemId1, cartItemId2);
    }

    @Test
    void shouldConsiderTwoCartItemIdsNotEqualWhenHavingDifferentUuids() {
        CartItemId cartItemId1 = CartItemId.newId();
        CartItemId cartItemId2 = CartItemId.newId();

        assertNotEquals(cartItemId1, cartItemId2);
    }
}

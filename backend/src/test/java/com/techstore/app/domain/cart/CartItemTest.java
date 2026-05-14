package com.techstore.app.domain.cart;

import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Quantity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CartItemTest {

    @Test
    void ensureDefaultConstructorLeavesFieldsNull() {
        CartItem cartItem = new CartItem();

        assertNull(cartItem.getId());
        assertNull(cartItem.getQuantity());
        assertNull(cartItem.getProduct());
        assertNull(cartItem.getCreatedAt());
        assertNull(cartItem.getUpdatedAt());
    }

    @Test
    void ensureConstructorInitializesProvidedFields() {
        Product mockProduct = mock(Product.class);
        Integer quantity = 5;

        CartItem cartItem = new CartItem(quantity, mockProduct);

        assertNotNull(cartItem.getId());
        assertNotNull(cartItem.getQuantity());
        assertEquals(quantity, cartItem.getQuantity().getQuantity());
        assertEquals(mockProduct, cartItem.getProduct());
        assertNull(cartItem.getCreatedAt());
        assertNull(cartItem.getUpdatedAt());
    }

    @Test
    void ensureConstructorCreatesValidQuantity() {
        Product mockProduct = mock(Product.class);
        Integer quantity = 10;

        CartItem cartItem = new CartItem(quantity, mockProduct);

        assertNotNull(cartItem.getQuantity());
        assertEquals(quantity, cartItem.getQuantity().getQuantity());
    }

    @Test
    void ensureOnCreateSetsCreatedAndUpdatedAt() {
        CartItem cartItem = new CartItem();

        cartItem.onCreate();

        assertNotNull(cartItem.getCreatedAt());
        assertNotNull(cartItem.getUpdatedAt());
        assertEquals(cartItem.getCreatedAt(), cartItem.getUpdatedAt());
    }

    @Test
    void ensureOnUpdateRefreshesOnlyUpdatedAt() {
        CartItem cartItem = new CartItem();
        cartItem.onCreate();
        LocalDateTime createdAt = cartItem.getCreatedAt();
        LocalDateTime firstUpdatedAt = cartItem.getUpdatedAt();

        cartItem.onUpdate();

        assertEquals(createdAt, cartItem.getCreatedAt());
        assertNotNull(cartItem.getUpdatedAt());
        assertFalse(cartItem.getUpdatedAt().isBefore(firstUpdatedAt));
    }

    @Test
    void ensureCartItemWithSameIdAndQuantityAndProductAreEqual() {
        Product mockProduct = mock(Product.class);
        CartItem cartItem1 = new CartItem(5, mockProduct);
        CartItem cartItem2 = new CartItem(5, mockProduct);

        // Both have their own IDs, so they won't be equal unless IDs are manually set
        assertNotEquals(cartItem1, cartItem2);
    }
}

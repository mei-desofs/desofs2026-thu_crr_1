package com.techstore.app.domain.cart;

import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("CartItem Domain Tests")
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
        assertNotEquals(cartItem1, cartItem2);
    }

    @Test
    @DisplayName("CartItem: incrementQuantity com valor válido")
    void ensureIncrementQuantityWorksWithValidValue() {
        Product mockProduct = mock(Product.class);
        CartItem cartItem = new CartItem(5, mockProduct);
        cartItem.getQuantity().incrementQuantity(3);
        assertEquals(8, cartItem.getQuantity().getQuantity());
    }

    @Test
    @DisplayName("CartItem: decrementQuantity com valor válido")
    void ensureDecrementQuantityWorksWithValidValue() {
        Product mockProduct = mock(Product.class);
        CartItem cartItem = new CartItem(5, mockProduct);
        cartItem.getQuantity().decrementQuantity(2);
        assertEquals(3, cartItem.getQuantity().getQuantity());
    }

    @Test
    @DisplayName("CartItem: incrementQuantity com valor inválido (negativo)")
    void ensureIncrementQuantityFailsWithNegativeValue() {
        Product mockProduct = mock(Product.class);
        CartItem cartItem = new CartItem(5, mockProduct);
        assertThrows(Exception.class, () -> cartItem.getQuantity().incrementQuantity(-1));
    }

    @Test
    @DisplayName("CartItem: decrementQuantity com valor inválido (negativo)")
    void ensureDecrementQuantityFailsWithNegativeValue() {
        Product mockProduct = mock(Product.class);
        CartItem cartItem = new CartItem(5, mockProduct);
        assertThrows(Exception.class, () -> cartItem.getQuantity().decrementQuantity(-1));
    }

    @Test
    @DisplayName("CartItem: decrementQuantity com resultado negativo")
    void ensureDecrementQuantityFailsWhenResultIsNegative() {
        Product mockProduct = mock(Product.class);
        CartItem cartItem = new CartItem(2, mockProduct);
        assertThrows(Exception.class, () -> cartItem.getQuantity().decrementQuantity(5));
    }

    @Test
    @DisplayName("CartItem: attachTo cart")
    void ensureAttachToCartWorks() {
        Product mockProduct = mock(Product.class);
        CartItem cartItem = new CartItem(3, mockProduct);
        Cart mockCart = mock(Cart.class);
        cartItem.attachTo(mockCart);
        assertEquals(mockCart, cartItem.getCart());
    }

    @Test
    @DisplayName("CartItem: attachTo falha se já está em cart")
    void ensureAttachToFailsIfAlreadyAttached() {
        Product mockProduct = mock(Product.class);
        CartItem cartItem = new CartItem(3, mockProduct);
        Cart mockCart1 = mock(Cart.class);
        Cart mockCart2 = mock(Cart.class);
        cartItem.attachTo(mockCart1);
        assertThrows(IllegalStateException.class, () -> cartItem.attachTo(mockCart2));
    }
}
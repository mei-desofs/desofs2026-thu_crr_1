package com.techstore.app.domain.cart;

import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.product.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("Cart Domain Tests")
class CartTest {

    @Test
    void ensureDefaultConstructorLeavesFieldsNull() {
        Cart cart = new Cart();
        assertNull(cart.getId());
        assertNull(cart.getItems());
        assertNull(cart.getCustomer());
        assertNull(cart.getCreatedAt());
        assertNull(cart.getUpdatedAt());
    }

    @Test
    void ensureConstructorInitializesProvidedFields() {
        Product mockProduct = mock(Product.class);
        Customer mockCustomer = mock(Customer.class);
        CartItem cartItem = new CartItem(3, mockProduct);
        Cart cart = new Cart(mockCustomer);
        cart.addItem(cartItem);
        assertNotNull(cart.getId());
        assertNotNull(cart.getItems());
        assertEquals(1, cart.getItems().size());
        assertEquals(cartItem, cart.getItems().get(0));
        assertNull(cart.getCreatedAt());
        assertNull(cart.getUpdatedAt());
    }

    @Test
    void ensureConstructorCreatesNewCartId() {
        Product mockProduct = mock(Product.class);
        Customer mockCustomer = mock(Customer.class);
        CartItem cartItem = new CartItem(2, mockProduct);
        List<CartItem> items = new ArrayList<>();
        items.add(cartItem);
        Cart cart1 = new Cart(mockCustomer);
        Cart cart2 = new Cart(mockCustomer);
        assertNotEquals(cart1.getId(), cart2.getId());
    }

    @Test
    void ensureConstructorWithEmptyItemsList() {
        Customer mockCustomer = mock(Customer.class);
        List<CartItem> items = new ArrayList<>();
        Cart cart = new Cart(mockCustomer);
        assertNotNull(cart.getId());
        assertEquals(items, cart.getItems());
    }

    @Test
    void ensureOnCreateSetsCreatedAndUpdatedAt() {
        Cart cart = new Cart();
        cart.onCreate();
        assertNotNull(cart.getCreatedAt());
        assertNotNull(cart.getUpdatedAt());
        assertEquals(cart.getCreatedAt(), cart.getUpdatedAt());
    }

    @Test
    void ensureOnUpdateRefreshesOnlyUpdatedAt() {
        Cart cart = new Cart();
        cart.onCreate();
        LocalDateTime createdAt = cart.getCreatedAt();
        LocalDateTime firstUpdatedAt = cart.getUpdatedAt();
        cart.onUpdate();
        assertEquals(createdAt, cart.getCreatedAt());
        assertNotNull(cart.getUpdatedAt());
        assertFalse(cart.getUpdatedAt().isBefore(firstUpdatedAt));
    }

    @Test
    void ensureCartWithMultipleItems() {
        Product mockProduct1 = mock(Product.class);
        Product mockProduct2 = mock(Product.class);
        Customer mockCustomer = mock(Customer.class);
        CartItem item1 = new CartItem(2, mockProduct1);
        CartItem item2 = new CartItem(3, mockProduct2);
        Cart cart = new Cart(mockCustomer);
        cart.addItem(item1);
        cart.addItem(item2);
        assertNotNull(cart.getId());
        assertEquals(2, cart.getItems().size());
        assertTrue(cart.getItems().contains(item1));
        assertTrue(cart.getItems().contains(item2));
    }

    @Test
    void ensureCartCanHaveCustomer() {
        Product mockProduct = mock(Product.class);
        CartItem cartItem = new CartItem(1, mockProduct);
        List<CartItem> items = new ArrayList<>();
        items.add(cartItem);
        Customer mockCustomer = mock(Customer.class);
        Cart cart = new Cart(mockCustomer);
        assertNotNull(cart.getCustomer());
    }

    @Test
    @DisplayName("Cart: removeItem remove item com sucesso")
    void ensureRemoveItemWorksSuccessfully() {
        Product mockProduct1 = mock(Product.class);
        Product mockProduct2 = mock(Product.class);
        Customer mockCustomer = mock(Customer.class);
        CartItem item1 = new CartItem(2, mockProduct1);
        CartItem item2 = new CartItem(3, mockProduct2);
        Cart cart = new Cart(mockCustomer);
        cart.addItem(item1);
        cart.addItem(item2);
        assertEquals(2, cart.getItems().size());
        cart.getItems().remove(item1);
        assertEquals(1, cart.getItems().size());
        assertFalse(cart.getItems().contains(item1));
        assertTrue(cart.getItems().contains(item2));
    }

    @Test
    @DisplayName("Cart: removeItem deixa cart vazio")
    void ensureRemoveItemCanLeaveCartEmpty() {
        Product mockProduct = mock(Product.class);
        Customer mockCustomer = mock(Customer.class);
        CartItem item = new CartItem(5, mockProduct);
        Cart cart = new Cart(mockCustomer);
        cart.addItem(item);
        assertEquals(1, cart.getItems().size());
        cart.getItems().remove(item);
        assertEquals(0, cart.getItems().size());
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    @DisplayName("Cart: removeItem com múltiplos items remove apenas um")
    void ensureRemoveItemRemovesOnlySpecificItem() {
        Product mockProduct1 = mock(Product.class);
        Product mockProduct2 = mock(Product.class);
        Product mockProduct3 = mock(Product.class);
        Customer mockCustomer = mock(Customer.class);
        CartItem item1 = new CartItem(1, mockProduct1);
        CartItem item2 = new CartItem(2, mockProduct2);
        CartItem item3 = new CartItem(3, mockProduct3);
        Cart cart = new Cart(mockCustomer);
        cart.addItem(item1);
        cart.addItem(item2);
        cart.addItem(item3);   
        cart.getItems().remove(item2);
        assertEquals(2, cart.getItems().size());
        assertTrue(cart.getItems().contains(item1));
        assertFalse(cart.getItems().contains(item2));
        assertTrue(cart.getItems().contains(item3));
    }

    @Test
    @DisplayName("Cart: removeItem que não existe não muda lista")
    void ensureRemoveItemThatDoesNotExistDoesNothing() {
        Product mockProduct1 = mock(Product.class);
        Product mockProduct2 = mock(Product.class);
        Customer mockCustomer = mock(Customer.class);
        CartItem item1 = new CartItem(2, mockProduct1);
        CartItem itemNotInCart = new CartItem(3, mockProduct2);
        Cart cart = new Cart(mockCustomer);
        cart.addItem(item1);
        assertEquals(1, cart.getItems().size());
        boolean removed = cart.getItems().remove(itemNotInCart);
        assertFalse(removed);
        assertEquals(1, cart.getItems().size());
        assertTrue(cart.getItems().contains(item1));
    }

    @Test
    @DisplayName("Cart: removeItem mantém outros items intactos")
    void ensureRemoveItemDoesNotAffectOtherItems() {
        Product mockProduct1 = mock(Product.class);
        Product mockProduct2 = mock(Product.class);
        Customer mockCustomer = mock(Customer.class);
        CartItem item1 = new CartItem(5, mockProduct1);
        CartItem item2 = new CartItem(10, mockProduct2);
        Cart cart = new Cart(mockCustomer);
        cart.addItem(item1);
        cart.addItem(item2);
        cart.getItems().remove(item1);
        assertEquals(10, item2.getQuantity().getQuantity());
        assertEquals(mockProduct2, item2.getProduct());
    }

@Test
@DisplayName("Cart: permite adicionar novo item após remover")
void ensureCartCanAddNewItemAfterRemovingOne() {
    Product mockProduct1 = mock(Product.class);
    Product mockProduct2 = mock(Product.class);
    Customer mockCustomer = mock(Customer.class);
    CartItem item1 = new CartItem(3, mockProduct1);
    CartItem item2 = new CartItem(5, mockProduct2);
    Cart cart = new Cart(mockCustomer);
    cart.addItem(item1);
    assertEquals(1, cart.getItems().size());
    cart.getItems().remove(item1);
    assertEquals(0, cart.getItems().size());
    cart.addItem(item2);
    assertEquals(1, cart.getItems().size());
    assertTrue(cart.getItems().contains(item2));
}
}
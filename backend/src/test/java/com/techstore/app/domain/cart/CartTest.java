package com.techstore.app.domain.cart;

import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.order.OrderItem;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        Cart cart1 = new Cart( mockCustomer);
        Cart cart2 = new Cart( mockCustomer);

        assertNotEquals(cart1.getId(), cart2.getId());
    }

    @Test
    void ensureConstructorWithEmptyItemsList() {
        Customer mockCustomer = mock(Customer.class);
        List<CartItem> items = new ArrayList<>();

        Cart cart = new Cart( mockCustomer);

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

        Cart cart = new Cart( mockCustomer);
        assertNotNull(cart.getCustomer());
    }

    @Test
    void ensureCalculateTotalReturnsZeroWhenItemsIsNull() {
        Cart cart = new Cart();

        BigDecimal total = cart.calculateTotal();

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void ensureCalculateTotalReturnsZeroWhenItemsIsEmpty() {
        Customer mockCustomer = mock(Customer.class);

        Cart cart = new Cart(new ArrayList<>(), mockCustomer);

        BigDecimal total = cart.calculateTotal();

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void ensureCalculateTotalWithSingleItem() {
        Product mockProduct = mock(Product.class);
        Money mockPrice = mock(Money.class);
        Customer mockCustomer = mock(Customer.class);

        when(mockProduct.getPrice()).thenReturn(mockPrice);
        when(mockPrice.getMoneyValue()).thenReturn(new BigDecimal("10.50"));

        CartItem cartItem = new CartItem(2, mockProduct);
        Cart cart = new Cart(List.of(cartItem), mockCustomer);

        BigDecimal total = cart.calculateTotal();

        assertEquals(new BigDecimal("21.00"), total);
    }

    @Test
    void ensureCalculateTotalWithMultipleItems() {
        Product mockProduct1 = mock(Product.class);
        Product mockProduct2 = mock(Product.class);

        Money mockPrice1 = mock(Money.class);
        Money mockPrice2 = mock(Money.class);

        Customer mockCustomer = mock(Customer.class);

        when(mockProduct1.getPrice()).thenReturn(mockPrice1);
        when(mockProduct2.getPrice()).thenReturn(mockPrice2);

        when(mockPrice1.getMoneyValue()).thenReturn(new BigDecimal("10.00"));
        when(mockPrice2.getMoneyValue()).thenReturn(new BigDecimal("5.50"));

        CartItem item1 = new CartItem(2, mockProduct1);
        CartItem item2 = new CartItem(3, mockProduct2);

        Cart cart = new Cart(List.of(item1, item2), mockCustomer);

        BigDecimal total = cart.calculateTotal();

        assertEquals(new BigDecimal("36.50"), total);
    }

    @Test
    void ensureToOrderItemsReturnsEmptyListWhenItemsIsNull() {
        Cart cart = new Cart();

        List<OrderItem> orderItems = cart.toOrderItems();

        assertNotNull(orderItems);
        assertTrue(orderItems.isEmpty());
    }

    @Test
    void ensureToOrderItemsConvertsCartItemsToOrderItems() {
        Product mockProduct = mock(Product.class);
        Money mockPrice = mock(Money.class);
        Customer mockCustomer = mock(Customer.class);

        when(mockProduct.getPrice()).thenReturn(mockPrice);
        when(mockPrice.getMoneyValue()).thenReturn(new BigDecimal("15.99"));

        CartItem cartItem = new CartItem(4, mockProduct);
        Cart cart = new Cart(List.of(cartItem), mockCustomer);

        List<OrderItem> orderItems = cart.toOrderItems();

        assertNotNull(orderItems);
        assertEquals(1, orderItems.size());

        OrderItem orderItem = orderItems.get(0);

        assertEquals(4, orderItem.getQuantity().getQuantity());
        assertEquals(new BigDecimal("15.99"), orderItem.getPrice().getMoneyValue());
        assertEquals(mockProduct, orderItem.getProduct());
    }

    @Test
    void ensureToOrderItemsConvertsMultipleCartItemsToOrderItems() {
        Product mockProduct1 = mock(Product.class);
        Product mockProduct2 = mock(Product.class);

        Money mockPrice1 = mock(Money.class);
        Money mockPrice2 = mock(Money.class);

        Customer mockCustomer = mock(Customer.class);

        when(mockProduct1.getPrice()).thenReturn(mockPrice1);
        when(mockProduct2.getPrice()).thenReturn(mockPrice2);

        when(mockPrice1.getMoneyValue()).thenReturn(new BigDecimal("20.00"));
        when(mockPrice2.getMoneyValue()).thenReturn(new BigDecimal("7.25"));

        CartItem item1 = new CartItem(1, mockProduct1);
        CartItem item2 = new CartItem(5, mockProduct2);

        Cart cart = new Cart(List.of(item1, item2), mockCustomer);

        List<OrderItem> orderItems = cart.toOrderItems();

        assertNotNull(orderItems);
        assertEquals(2, orderItems.size());

        assertEquals(1, orderItems.get(0).getQuantity().getQuantity());
        assertEquals(new BigDecimal("20.00"), orderItems.get(0).getPrice().getMoneyValue());
        assertEquals(mockProduct1, orderItems.get(0).getProduct());

        assertEquals(5, orderItems.get(1).getQuantity().getQuantity());
        assertEquals(new BigDecimal("7.25"), orderItems.get(1).getPrice().getMoneyValue());
        assertEquals(mockProduct2, orderItems.get(1).getProduct());
    }
}

package com.techstore.app.domain.order;

import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.domain.shared.Quantity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OrderItemTest {

    @Test
    void ensureDefaultConstructorLeavesFieldsNull() {
        OrderItem orderItem = new OrderItem();

        assertNull(orderItem.getId());
        assertNull(orderItem.getQuantity());
        assertNull(orderItem.getPrice());
        assertNull(orderItem.getProduct());
        assertNull(orderItem.getCreatedAt());
        assertNull(orderItem.getUpdatedAt());
    }

    @Test
    void ensureConstructorInitializesProvidedFields() {
        Product mockProduct = mock(Product.class);
        Integer quantity = 3;
        BigDecimal price = new BigDecimal("99.99");

        OrderItem orderItem = new OrderItem(quantity, price, mockProduct);

        assertNotNull(orderItem.getId());
        assertNotNull(orderItem.getQuantity());
        assertEquals(quantity, orderItem.getQuantity().getQuantity());
        assertNotNull(orderItem.getPrice());
        assertEquals(price, orderItem.getPrice().getMoneyValue());
        assertEquals(mockProduct, orderItem.getProduct());
        assertNull(orderItem.getCreatedAt());
        assertNull(orderItem.getUpdatedAt());
    }

    @Test
    void ensureConstructorCreatesValidQuantity() {
        Product mockProduct = mock(Product.class);
        Integer quantity = 5;
        BigDecimal price = new BigDecimal("50.00");

        OrderItem orderItem = new OrderItem(quantity, price, mockProduct);

        assertNotNull(orderItem.getQuantity());
        assertEquals(quantity, orderItem.getQuantity().getQuantity());
    }

    @Test
    void ensureConstructorCreatesValidPrice() {
        Product mockProduct = mock(Product.class);
        Integer quantity = 2;
        BigDecimal price = new BigDecimal("199.99");

        OrderItem orderItem = new OrderItem(quantity, price, mockProduct);

        assertNotNull(orderItem.getPrice());
        assertEquals(price, orderItem.getPrice().getMoneyValue());
    }

    @Test
    void ensureOnCreateSetsCreatedAndUpdatedAt() {
        OrderItem orderItem = new OrderItem();

        orderItem.onCreate();

        assertNotNull(orderItem.getCreatedAt());
        assertNotNull(orderItem.getUpdatedAt());
        assertEquals(orderItem.getCreatedAt(), orderItem.getUpdatedAt());
    }

    @Test
    void ensureOnUpdateRefreshesOnlyUpdatedAt() {
        OrderItem orderItem = new OrderItem();
        orderItem.onCreate();
        LocalDateTime createdAt = orderItem.getCreatedAt();
        LocalDateTime firstUpdatedAt = orderItem.getUpdatedAt();

        orderItem.onUpdate();

        assertEquals(createdAt, orderItem.getCreatedAt());
        assertNotNull(orderItem.getUpdatedAt());
        assertFalse(orderItem.getUpdatedAt().isBefore(firstUpdatedAt));
    }

    @Test
    void ensureOrderItemWithDifferentIdsAreNotEqual() {
        Product mockProduct = mock(Product.class);
        OrderItem orderItem1 = new OrderItem(5, new BigDecimal("99.99"), mockProduct);
        OrderItem orderItem2 = new OrderItem(5, new BigDecimal("99.99"), mockProduct);

        assertNotEquals(orderItem1, orderItem2);
    }
}

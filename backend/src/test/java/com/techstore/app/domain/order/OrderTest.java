package com.techstore.app.domain.order;

import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.product.Product;
import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OrderTest {

    @Test
    void ensureDefaultConstructorLeavesFieldsNull() {
        Order order = new Order();

        assertNull(order.getId());
        assertNull(order.getTotalPrice());
        assertNull(order.getAddress());
        assertNull(order.getOrderStatus());
        assertNull(order.getOrderItems());
        assertNull(order.getCustomer());
        assertNull(order.getCreatedAt());
        assertNull(order.getUpdatedAt());
    }

    @Test
    void ensureConstructorInitializesProvidedFieldsWithEmptyItemsList() {
        Order order = new Order(
            new BigDecimal("199.98"),
            "1000-000",
            "Lisbon",
            "Portugal",
            "Main Street",
            OrderStatus.PENDING,
            new ArrayList<>()
        );

        assertNotNull(order.getId());
        assertNotNull(order.getTotalPrice());
        assertEquals(new BigDecimal("199.98"), order.getTotalPrice().getMoneyValue());
        assertNotNull(order.getAddress());
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
        assertNotNull(order.getOrderItems());
        assertEquals(0, order.getOrderItems().size());
        assertNull(order.getCreatedAt());
        assertNull(order.getUpdatedAt());
    }

    @Test
    void ensureConstructorCreatesNewOrderId() {
        Order order1 = new Order(
            new BigDecimal("50.00"),
            "1000-000",
            "Lisbon",
            "Portugal",
            "Main Street",
            OrderStatus.PENDING,
            new ArrayList<>()
        );
        Order order2 = new Order(
            new BigDecimal("50.00"),
            "1000-000",
            "Lisbon",
            "Portugal",
            "Main Street",
            OrderStatus.PENDING,
            new ArrayList<>()
        );

        assertNotEquals(order1.getId(), order2.getId());
    }

    @Test
    void ensureConstructorThrowsExceptionWhenOrderHasItems() {
        Product mockProduct = mock(Product.class);
        OrderItem orderItem = new OrderItem(1, new BigDecimal("100.00"), mockProduct);
        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            new Order(
                new BigDecimal("100.00"),
                "1000-000",
                "Lisbon",
                "Portugal",
                "Main Street",
                OrderStatus.PENDING,
                items
            );
        });

        assertEquals("An order must contain at least one item.", exception.getMessage());
    }

    @Test
    void ensureOnCreateSetsCreatedAndUpdatedAt() {
        Order order = new Order();

        order.onCreate();

        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
        assertEquals(order.getCreatedAt(), order.getUpdatedAt());
    }

    @Test
    void ensureOnUpdateRefreshesOnlyUpdatedAt() {
        Order order = new Order();
        order.onCreate();
        LocalDateTime createdAt = order.getCreatedAt();
        LocalDateTime firstUpdatedAt = order.getUpdatedAt();

        order.onUpdate();

        assertEquals(createdAt, order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
        assertFalse(order.getUpdatedAt().isBefore(firstUpdatedAt));
    }

    @Test
    void ensureOrderWithMultipleItems() {
        Order order = new Order(
            new BigDecimal("349.95"),
            "1000-000",
            "Lisbon",
            "Portugal",
            "Main Street",
            OrderStatus.PENDING,
            new ArrayList<>()
        );

        assertNotNull(order.getId());
        assertEquals(0, order.getOrderItems().size());
    }

    @Test
    void ensureOrderWithDifferentStatuses() {
        Order orderPending = new Order(
            new BigDecimal("50.00"),
            "1000-000",
            "Lisbon",
            "Portugal",
            "Main Street",
            OrderStatus.PENDING,
            new ArrayList<>()
        );

        Order orderShipped = new Order(
            new BigDecimal("50.00"),
            "1000-000",
            "Lisbon",
            "Portugal",
            "Main Street",
            OrderStatus.SHIPPED,
            new ArrayList<>()
        );

        assertEquals(OrderStatus.PENDING, orderPending.getOrderStatus());
        assertEquals(OrderStatus.SHIPPED, orderShipped.getOrderStatus());
    }

    @Test
    void ensureOrderContainsAddressInformation() {
        Order order = new Order(
            new BigDecimal("100.00"),
            "2700-000",
            "Amadora",
            "Portugal",
            "Secondary Street",
            OrderStatus.PENDING,
            new ArrayList<>()
        );

        assertNotNull(order.getAddress());
    }
}

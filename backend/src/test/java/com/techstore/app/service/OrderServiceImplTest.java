package com.techstore.app.service;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartId;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.customer.CustomerId;
import com.techstore.app.domain.order.Order;
import com.techstore.app.domain.order.OrderItem;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.User;
import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.OrderResponseDTO;
import com.techstore.app.dto.shared.AddAddressDTO;
import com.techstore.app.logger.OrderAuditLogger;
import com.techstore.app.repository.CartRepository;
import com.techstore.app.repository.CustomerRepository;
import com.techstore.app.repository.OrderRepository;
import com.techstore.app.service.interfaces.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderAuditLogger orderAuditLogger;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void shouldCreateOrderAndReturnResponse() {
        UUID cartUuid = UUID.randomUUID();
        UUID customerUuid = UUID.randomUUID();

        CreateOrderRequestDTO request = mockCompleteCreateOrderRequest(cartUuid, customerUuid);

        Cart cart = mock(Cart.class);
        Customer customer = mock(Customer.class);
        User user = mock(User.class);

        CustomerId customerId = CustomerId.fromString(customerUuid.toString());

        Product product = mock(Product.class);
        OrderItem orderItem = new OrderItem(1, new BigDecimal("99.99"), product);
        List<OrderItem> orderItems = List.of(orderItem);

        when(cartRepository.findById(CartId.fromString(cartUuid.toString())))
                .thenReturn(Optional.of(cart));

        when(customerRepository.findById(CustomerId.fromString(customerUuid.toString())))
                .thenReturn(Optional.of(customer));

        when(cart.toOrderItems()).thenReturn(orderItems);
        when(cart.calculateTotal()).thenReturn(new BigDecimal("99.99"));

        when(customer.getId()).thenReturn(customerId);
        when(customer.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn(new Email("john@example.com"));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = orderService.createOrder(request);

        assertNotNull(response);
        assertNotNull(response.orderID());
        assertEquals(customerUuid.toString(), response.customerID());
        assertEquals(cartUuid.toString(), response.cartID());

        assertEquals("4000-001", response.address().postalCode());
        assertEquals("Porto", response.address().city());
        assertEquals("Portugal", response.address().country());
        assertEquals("Rua Teste", response.address().street());

        verify(cartRepository).findById(CartId.fromString(cartUuid.toString()));
        verify(customerRepository).findById(CustomerId.fromString(customerUuid.toString()));
        verify(cart).toOrderItems();
        verify(cart).calculateTotal();

        verify(orderRepository).save(any(Order.class));

        verify(cart).clearItems();
        verify(cartRepository).save(cart);

        verify(notificationService).sendOrderConfirmationEmail(
                eq("john@example.com"),
                contains(response.orderID()),
                contains(response.orderID())
        );

        verify(orderAuditLogger).logOrderCreationAttempt(request);
        verify(orderAuditLogger).logOrderCreation(
                response.orderID(),
                customerUuid.toString(),
                cartUuid.toString()
        );
    }

    @Test
    void shouldThrowWhenCartDoesNotExist() {
        UUID cartUuid = UUID.randomUUID();

        CreateOrderRequestDTO request = mockCreateOrderRequestWithCartIdOnly(cartUuid);

        when(cartRepository.findById(CartId.fromString(cartUuid.toString())))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.createOrder(request));

        assertEquals("Cart not found", exception.getMessage());

        verify(cartRepository).findById(CartId.fromString(cartUuid.toString()));
        verify(customerRepository, never()).findById(any(CustomerId.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartRepository, never()).save(any(Cart.class));
        verify(notificationService, never()).sendOrderConfirmationEmail(any(), any(), any());

        verify(orderAuditLogger).logOrderCreationAttempt(request);
        verify(orderAuditLogger).logOrderCreationFailure(eq(request), any(RuntimeException.class));
    }

    @Test
    void shouldThrowWhenCustomerDoesNotExist() {
        UUID cartUuid = UUID.randomUUID();
        UUID customerUuid = UUID.randomUUID();

        CreateOrderRequestDTO request = mockCreateOrderRequestWithIdsOnly(cartUuid, customerUuid);

        Cart cart = mock(Cart.class);

        when(cartRepository.findById(CartId.fromString(cartUuid.toString())))
                .thenReturn(Optional.of(cart));

        when(customerRepository.findById(CustomerId.fromString(customerUuid.toString())))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.createOrder(request));

        assertEquals("Customer not found", exception.getMessage());

        verify(cartRepository).findById(CartId.fromString(cartUuid.toString()));
        verify(customerRepository).findById(CustomerId.fromString(customerUuid.toString()));
        verify(cart, never()).toOrderItems();
        verify(cart, never()).calculateTotal();
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartRepository, never()).save(any(Cart.class));
        verify(notificationService, never()).sendOrderConfirmationEmail(any(), any(), any());

        verify(orderAuditLogger).logOrderCreationAttempt(request);
        verify(orderAuditLogger).logOrderCreationFailure(eq(request), any(RuntimeException.class));
    }

    @Test
    void shouldThrowWhenNotificationServiceFails() {
        UUID cartUuid = UUID.randomUUID();
        UUID customerUuid = UUID.randomUUID();

        CreateOrderRequestDTO request = mockCompleteCreateOrderRequest(cartUuid, customerUuid);

        Cart cart = mock(Cart.class);
        Customer customer = mock(Customer.class);
        User user = mock(User.class);

        Product product = mock(Product.class);
        OrderItem orderItem = new OrderItem(1, new BigDecimal("99.99"), product);
        List<OrderItem> orderItems = List.of(orderItem);

        when(cartRepository.findById(CartId.fromString(cartUuid.toString())))
                .thenReturn(Optional.of(cart));

        when(customerRepository.findById(CustomerId.fromString(customerUuid.toString())))
                .thenReturn(Optional.of(customer));

        when(cart.toOrderItems()).thenReturn(orderItems);
        when(cart.calculateTotal()).thenReturn(new BigDecimal("99.99"));

        when(customer.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn(new Email("john@example.com"));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RuntimeException emailException = new RuntimeException("Email failed");

        org.mockito.Mockito.doThrow(emailException)
                .when(notificationService)
                .sendOrderConfirmationEmail(any(), any(), any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.createOrder(request));

        assertEquals("Email failed", exception.getMessage());

        verify(orderRepository).save(any(Order.class));
        verify(cart).clearItems();
        verify(cartRepository).save(cart);
        verify(notificationService).sendOrderConfirmationEmail(any(), any(), any());

        verify(orderAuditLogger).logOrderCreationAttempt(request);
        verify(orderAuditLogger).logOrderCreationFailure(request, emailException);
        verify(orderAuditLogger, never()).logOrderCreation(any(), any(), any());
    }

    private CreateOrderRequestDTO mockCompleteCreateOrderRequest(UUID cartId, UUID customerId) {
        AddAddressDTO address = mock(AddAddressDTO.class);
        when(address.postalCode()).thenReturn("4000-001");
        when(address.city()).thenReturn("Porto");
        when(address.country()).thenReturn("Portugal");
        when(address.street()).thenReturn("Rua Teste");

        CreateOrderRequestDTO request = mock(CreateOrderRequestDTO.class);
        when(request.cartID()).thenReturn(cartId.toString());
        when(request.customerID()).thenReturn(customerId.toString());
        when(request.address()).thenReturn(address);

        return request;
    }

    private CreateOrderRequestDTO mockCreateOrderRequestWithIdsOnly(UUID cartId, UUID customerId) {
        CreateOrderRequestDTO request = mock(CreateOrderRequestDTO.class);
        when(request.cartID()).thenReturn(cartId.toString());
        when(request.customerID()).thenReturn(customerId.toString());

        return request;
    }

    private CreateOrderRequestDTO mockCreateOrderRequestWithCartIdOnly(UUID cartId) {
        CreateOrderRequestDTO request = mock(CreateOrderRequestDTO.class);
        when(request.cartID()).thenReturn(cartId.toString());

        return request;
    }
}
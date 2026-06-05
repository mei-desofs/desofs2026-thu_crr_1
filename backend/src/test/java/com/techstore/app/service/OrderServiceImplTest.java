package com.techstore.app.service;

import com.techstore.app.domain.carrier.Carrier;
import com.techstore.app.domain.carrier.CarrierId;
import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartId;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.customer.CustomerId;
import com.techstore.app.domain.order.Order;
import com.techstore.app.domain.order.OrderId;
import com.techstore.app.domain.order.OrderItem;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Address;
import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.domain.user.User;
import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.OrderResponseDTO;
import com.techstore.app.dto.order.OrderSummaryDTO;
import com.techstore.app.dto.shared.AddAddressDTO;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.logger.OrderAuditLogger;
import com.techstore.app.repository.CarrierRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CarrierRepository carrierRepository;

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

        doThrow(emailException)
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

    @Test
    void shouldReturnOrdersForCustomer() {

        UUID customerUuid = UUID.randomUUID();

        Customer customer = mock(Customer.class);

        CustomerId customerId = mock(CustomerId.class);

        when(customerId.getId()).thenReturn(customerUuid);

        when(customer.getId()).thenReturn(customerId);


        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        when(order1.getCustomer()).thenReturn(customer);
        when(order2.getCustomer()).thenReturn(customer);

        when(order1.getCarrier()).thenReturn(null);
        when(order2.getCarrier()).thenReturn(null);

        OrderId orderId = mock(OrderId.class);

        when(orderId.getId()).thenReturn(UUID.randomUUID());

        when(order1.getId()).thenReturn(orderId);
        when(order2.getId()).thenReturn(orderId);

        Address address = mock(Address.class);

        when(order1.getAddress()).thenReturn(address);
        when(order2.getAddress()).thenReturn(address);

        when(address.getPostalCode()).thenReturn("4000-001");
        when(address.getCity()).thenReturn("Porto");
        when(address.getCountry()).thenReturn("Portugal");
        when(address.getStreet()).thenReturn("Rua Teste");


        when(customerRepository.findById(CustomerId.fromString(customerUuid.toString()))).
                thenReturn(Optional.of(customer));

        when(orderRepository.findByCustomer(customer))
                .thenReturn(List.of(order1, order2));

        List<OrderSummaryDTO> response = orderService.getOrdersByCustomer(customerUuid.toString());

        assertEquals(2, response.size());

        verify(orderAuditLogger).logCustomerOrdersListingAttempt(customerUuid.toString());

        verify(orderAuditLogger).logCustomerOrdersListingSuccess(customerUuid.toString(), 2);
    }
    @Test
    void shouldThrowWhenCustomerNotFound() {

        UUID customerUuid = UUID.randomUUID();

        when(customerRepository.findById(CustomerId.fromString(customerUuid.toString())))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.getOrdersByCustomer(customerUuid.toString()));

        assertEquals("Customer not found", exception.getMessage());

        verify(orderAuditLogger).logCustomerOrdersListingAttempt(customerUuid.toString());

        verify(orderAuditLogger).logCustomerOrdersListingFailure(eq(customerUuid.toString()), any(RuntimeException.class));
    }

    @Test
    void shouldReturnOrdersForCarrier() {

        Customer customer = mock(Customer.class);

        CustomerId customerId = mock(CustomerId.class);

        when(customerId.getId()).thenReturn(UUID.randomUUID());

        when(customer.getId()).thenReturn(customerId);

        UUID carrierUuid = UUID.randomUUID();

        Carrier carrier = mock(Carrier.class);

        CarrierId carrierId = mock(CarrierId.class);

        when(carrierId.getId()).thenReturn(carrierUuid);

        when(carrier.getId()).thenReturn(carrierId);

        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        when(order1.getCustomer()).thenReturn(customer);
        when(order2.getCustomer()).thenReturn(customer);

        when(order1.getCarrier()).thenReturn(carrier);
        when(order2.getCarrier()).thenReturn(carrier);

        OrderId orderId = mock(OrderId.class);

        when(orderId.getId()).thenReturn(UUID.randomUUID());

        when(order1.getId()).thenReturn(orderId);
        when(order2.getId()).thenReturn(orderId);

        Address address = mock(Address.class);

        when(order1.getAddress()).thenReturn(address);
        when(order2.getAddress()).thenReturn(address);

        when(address.getPostalCode()).thenReturn("4000-001");
        when(address.getCity()).thenReturn("Porto");
        when(address.getCountry()).thenReturn("Portugal");
        when(address.getStreet()).thenReturn("Rua Teste");


        when(carrierRepository.findById(CarrierId.fromString(carrierUuid.toString()))).
                thenReturn(Optional.of(carrier));

        when(orderRepository.findByCarrier(carrier))
                .thenReturn(List.of(order1, order2));

        List<OrderSummaryDTO> response = orderService.getOrdersByCarrier(carrierUuid.toString());

        assertEquals(2, response.size());

        verify(orderAuditLogger).logCarrierOrdersListingAttempt(carrierUuid.toString());

        verify(orderAuditLogger).logCarrierOrdersListingSuccess(carrierUuid.toString(), 2);
    }
    @Test
    void shouldThrowWhenCarrierNotFound() {

        UUID carrierUuid = UUID.randomUUID();

        when(carrierRepository.findById(CarrierId.fromString(carrierUuid.toString())))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.getOrdersByCarrier(carrierUuid.toString()));

        assertEquals("Carrier not found", exception.getMessage());

        verify(orderAuditLogger).logCarrierOrdersListingAttempt(carrierUuid.toString());

        verify(orderAuditLogger).logCarrierOrdersListingFailure(eq(carrierUuid.toString()), any(RuntimeException.class));
    }

    @Test
    void shouldPickupOrderAndSave() {
        String supabaseUserId = UUID.randomUUID().toString();
        String orderIdUUID = UUID.randomUUID().toString();

        Order order = mock(Order.class);
        Carrier carrier = mock(Carrier.class);

        when(orderRepository.findById(OrderId.fromString(orderIdUUID)))
                .thenReturn(Optional.of(order));
        when(carrierRepository.findByUserSupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(carrier));

        orderService.pickupOrder(orderIdUUID, supabaseUserId);


        verify(order).pickup(carrier);

        verify(orderRepository).save(order);


        verify(orderAuditLogger).logPickupAttempt(orderIdUUID, supabaseUserId);
        verify(orderAuditLogger).logPickupSuccess(orderIdUUID, supabaseUserId);
        verify(orderAuditLogger, never()).logPickupFailure(any(), any(), any());
    }

    @Test
    void shouldThrowAndLogFailureWhenOrderNotFound() {
        String supabaseUserId = UUID.randomUUID().toString();
        String orderIdUUID = UUID.randomUUID().toString();

        when(orderRepository.findById(OrderId.fromString(orderIdUUID)))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.pickupOrder(orderIdUUID, supabaseUserId));

        assertEquals("Order not found", ex.getMessage());

        verify(orderAuditLogger).logPickupAttempt(orderIdUUID, supabaseUserId);
        verify(orderAuditLogger).logPickupFailure(eq(orderIdUUID), eq(supabaseUserId), any());
        verify(orderAuditLogger, never()).logPickupSuccess(any(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowAndLogFailureWhenCarrierProfileNotFound() {
        String supabaseUserId = UUID.randomUUID().toString();
        String orderIdUUID = UUID.randomUUID().toString();

        Order order = mock(Order.class);
        when(orderRepository.findById(OrderId.fromString(orderIdUUID)))
                .thenReturn(Optional.of(order));
        when(carrierRepository.findByUserSupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.pickupOrder(orderIdUUID, supabaseUserId));

        assertEquals("Carrier not found", ex.getMessage());

        verify(orderAuditLogger).logPickupAttempt(orderIdUUID, supabaseUserId);
        verify(orderAuditLogger).logPickupFailure(eq(orderIdUUID), eq(supabaseUserId), any());
        verify(order, never()).pickup(any());
        verify(orderRepository, never()).save(any());
    }
    @Test
    void shouldThrowAndLogFailureWhenDomainRuleViolated() {
        String supabaseUserId = UUID.randomUUID().toString();
        String orderIdUUID = UUID.randomUUID().toString();

        Order order = mock(Order.class);
        Carrier carrier = mock(Carrier.class);


        when(orderRepository.findById(OrderId.fromString(orderIdUUID)))
                .thenReturn(Optional.of(order));
        when(carrierRepository.findByUserSupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(carrier));

        // Simulate domain rejecting the transition (e.g. already PICKED_UP)
        doThrow(new BusinessException("Order cannot be picked up: current status is PICKED_UP"))
                .when(order).pickup(carrier);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.pickupOrder(orderIdUUID, supabaseUserId));

        assertTrue(ex.getMessage().contains("PICKED_UP"));

        verify(orderAuditLogger).logPickupAttempt(orderIdUUID, supabaseUserId);
        verify(orderAuditLogger).logPickupFailure(eq(orderIdUUID), eq(supabaseUserId), any());
        verify(orderRepository, never()).save(any());
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
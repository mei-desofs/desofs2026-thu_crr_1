package com.techstore.app.service;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartId;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.customer.CustomerId;
import com.techstore.app.domain.order.Order;
import com.techstore.app.domain.order.OrderId;
import com.techstore.app.domain.order.OrderItem;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Address;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.domain.user.User;
import com.techstore.app.domain.user.UserId;
import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.OrderResponseDTO;
import com.techstore.app.dto.order.OrderSummaryDTO;
import com.techstore.app.dto.shared.AddAddressDTO;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.logger.OrderAuditLogger;
import com.techstore.app.repository.CartRepository;
import com.techstore.app.repository.CustomerRepository;
import com.techstore.app.repository.OrderRepository;
import com.techstore.app.repository.UserRepository;
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
    private UserRepository userRepository;

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
        String supabaseUserId = UUID.randomUUID().toString();
        UUID userUuid = UUID.randomUUID();

        CreateOrderRequestDTO request = mockCompleteCreateOrderRequest(cartUuid);

        Cart cart = mock(Cart.class);
        Customer customer = mock(Customer.class);
        User user = mock(User.class);

        UserId userId = mock(UserId.class);
        when(userId.getId()).thenReturn(userUuid);
        when(user.getId()).thenReturn(userId);

        CustomerId customerId = CustomerId.fromString(customerUuid.toString());

        Product product = mock(Product.class);
        OrderItem orderItem = new OrderItem(1, new BigDecimal("99.99"), product);
        List<OrderItem> orderItems = List.of(orderItem);

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);

        when(cartRepository.findById(CartId.fromString(cartUuid.toString())))
                .thenReturn(Optional.of(cart));

        when(customerRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(customer));

        when(cart.toOrderItems()).thenReturn(orderItems);
        when(cart.calculateTotal()).thenReturn(new BigDecimal("99.99"));

        when(customer.getId()).thenReturn(customerId);
        when(customer.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn(new Email("john@example.com"));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = orderService.createOrder(request,supabaseUserId);

        assertNotNull(response);
        assertNotNull(response.orderID());
        assertEquals(customerUuid.toString(), response.customerID());
        assertEquals(cartUuid.toString(), response.cartID());

        assertEquals("4000-001", response.address().postalCode());
        assertEquals("Porto", response.address().city());
        assertEquals("Portugal", response.address().country());
        assertEquals("Rua Teste", response.address().street());

        verify(cartRepository).findById(CartId.fromString(cartUuid.toString()));
        verify(customerRepository).findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId));
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

        verify(orderAuditLogger).logOrderCreationAttempt(request,userUuid.toString());
        verify(orderAuditLogger).logOrderCreation(
                response.orderID(),
                userUuid.toString(),
                cartUuid.toString()
        );
    }

    @Test
    void shouldThrowWhenCartDoesNotExist() {
        UUID cartUuid = UUID.randomUUID();
        String supabaseUserId = UUID.randomUUID().toString();
        UUID userUuid = UUID.randomUUID();

        User user = mock(User.class);

        UserId userId = mock(UserId.class);
        when(userId.getId()).thenReturn(userUuid);

        when(user.getId()).thenReturn(userId);

        CreateOrderRequestDTO request = mockCreateOrderRequestWithCartIdOnly(cartUuid);

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);

        when(cartRepository.findById(CartId.fromString(cartUuid.toString())))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.createOrder(request,supabaseUserId));

        assertEquals("Cart not found", exception.getMessage());

        verify(cartRepository).findById(CartId.fromString(cartUuid.toString()));
        verify(customerRepository, never()).findBySupabaseUserId(any(SupabaseUserId.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartRepository, never()).save(any(Cart.class));
        verify(notificationService, never()).sendOrderConfirmationEmail(any(), any(), any());

        verify(orderAuditLogger).logOrderCreationAttempt(request,userUuid.toString());
        verify(orderAuditLogger).logOrderCreationFailure(eq(request),eq(userUuid.toString()), any(RuntimeException.class));
    }

    @Test
    void shouldThrowWhenCustomerDoesNotExist() {
        UUID cartUuid = UUID.randomUUID();
        UUID customerUuid = UUID.randomUUID();
        String supabaseUserId = UUID.randomUUID().toString();
        UUID userUuid = UUID.randomUUID();

        User user = mock(User.class);

        UserId userId = mock(UserId.class);
        when(userId.getId()).thenReturn(userUuid);
        when(user.getId()).thenReturn(userId);

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);

        CreateOrderRequestDTO request = mockCreateOrderRequestWithIdsOnly(cartUuid);

        Cart cart = mock(Cart.class);

        when(cartRepository.findById(CartId.fromString(cartUuid.toString())))
                .thenReturn(Optional.of(cart));

        when(customerRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.createOrder(request,supabaseUserId));

        assertEquals("Customer not found", exception.getMessage());

        verify(cartRepository).findById(CartId.fromString(cartUuid.toString()));
        verify(customerRepository).findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId));
        verify(cart, never()).toOrderItems();
        verify(cart, never()).calculateTotal();
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartRepository, never()).save(any(Cart.class));
        verify(notificationService, never()).sendOrderConfirmationEmail(any(), any(), any());

        verify(orderAuditLogger).logOrderCreationAttempt(request,userUuid.toString());
        verify(orderAuditLogger).logOrderCreationFailure(eq(request),eq(userUuid.toString()), any(RuntimeException.class));
    }

    @Test
    void shouldThrowWhenNotificationServiceFails() {
        UUID cartUuid = UUID.randomUUID();
        String supabaseUserId = UUID.randomUUID().toString();
        UUID userUuid = UUID.randomUUID();

        User user = mock(User.class);

        UserId userId = mock(UserId.class);
        when(userId.getId()).thenReturn(userUuid);
        when(user.getId()).thenReturn(userId);


        CreateOrderRequestDTO request = mockCompleteCreateOrderRequest(cartUuid);

        Cart cart = mock(Cart.class);
        Customer customer = mock(Customer.class);

        Product product = mock(Product.class);
        OrderItem orderItem = new OrderItem(1, new BigDecimal("99.99"), product);
        List<OrderItem> orderItems = List.of(orderItem);

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);

        when(cartRepository.findById(CartId.fromString(cartUuid.toString())))
                .thenReturn(Optional.of(cart));

        when(customerRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
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

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.createOrder(request,supabaseUserId));

        assertEquals("Email failed", exception.getMessage());

        verify(orderRepository).save(any(Order.class));
        verify(cart).clearItems();
        verify(cartRepository).save(cart);
        verify(notificationService).sendOrderConfirmationEmail(any(), any(), any());

        verify(orderAuditLogger).logOrderCreationAttempt(request,userUuid.toString());
        verify(orderAuditLogger).logOrderCreationFailure(request,userUuid.toString(), emailException);
        verify(orderAuditLogger, never()).logOrderCreation(any(), any(), any());
    }

    @Test
    void shouldReturnOrdersForCustomer() {

        String supabaseUserId = UUID.randomUUID().toString();
        UUID userUuid = UUID.randomUUID();

        User user = mock(User.class);

        UserId userId = mock(UserId.class);

        when(userId.getId()).thenReturn(userUuid);
        when(user.getId()).thenReturn(userId);

        Customer customer = mock(Customer.class);

        CustomerId customerId = mock(CustomerId.class);

        when(customerId.getId()).thenReturn(UUID.randomUUID());

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

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);

        when(customerRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId))).
                thenReturn(Optional.of(customer));

        when(orderRepository.findByCustomer(customer))
                .thenReturn(List.of(order1, order2));

        List<OrderSummaryDTO> response = orderService.getOrdersByCustomer(supabaseUserId);

        assertEquals(2, response.size());

        verify(orderAuditLogger).logCustomerOrdersListingAttempt(userUuid.toString());

        verify(orderAuditLogger).logCustomerOrdersListingSuccess(userUuid.toString(), 2);
    }
    @Test
    void shouldThrowWhenCustomerNotFound() {

        String supabaseUserId = UUID.randomUUID().toString();
        UUID userUuid = UUID.randomUUID();

        User user = mock(User.class);

        UserId userId = mock(UserId.class);
        when(userId.getId()).thenReturn(userUuid);
        when(user.getId()).thenReturn(userId);

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);

        when(customerRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.getOrdersByCustomer(supabaseUserId));

        assertEquals("Customer not found", exception.getMessage());

        verify(orderAuditLogger).logCustomerOrdersListingAttempt(userUuid.toString());

        verify(orderAuditLogger).logCustomerOrdersListingFailure(eq(userUuid.toString()), any(RuntimeException.class));
    }

    @Test
    void shouldReturnOrdersForCarrier() {

        String supabaseUserId = UUID.randomUUID().toString();
        UUID userUuid = UUID.randomUUID();

        User user = mock(User.class);

        UserId userId = mock(UserId.class);
        when(userId.getId()).thenReturn(userUuid);

        Customer customer = mock(Customer.class);

        CustomerId customerId = mock(CustomerId.class);

        when(customerId.getId()).thenReturn(UUID.randomUUID());

        when(customer.getId()).thenReturn(customerId);

        UUID carrierUuid = UUID.randomUUID();

        User carrier = mock(User.class);

        UserId carrierId = mock(UserId.class);


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

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId))).
                thenReturn(Optional.of(carrier));
        when(carrier.getId()).thenReturn(userId);

        when(orderRepository.findByCarrier(carrier))
                .thenReturn(List.of(order1, order2));

        List<OrderSummaryDTO> response = orderService.getOrdersByCarrier(supabaseUserId);

        assertEquals(2, response.size());

        verify(orderAuditLogger).logCarrierOrdersListingAttempt(userUuid.toString());

        verify(orderAuditLogger).logCarrierOrdersListingSuccess(userUuid.toString(), 2);
    }
    @Test
    void shouldThrowWhenCarrierNotFound() {

        String supabaseUserId = UUID.randomUUID().toString();
        UUID userUuid = UUID.randomUUID();

        User user = mock(User.class);

        UserId userId = mock(UserId.class);

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.getOrdersByCarrier(supabaseUserId));

        assertEquals("User not found", exception.getMessage());

        }

    @Test
    void shouldPickupOrderAndSave() {
        String supabaseUserId = UUID.randomUUID().toString();
        String orderIdUUID = UUID.randomUUID().toString();
        UUID userUuid = UUID.randomUUID();

        Address address = mock(Address.class);
        when(address.getCity()).thenReturn("Porto");
        when(address.getCountry()).thenReturn("Portugal");
        when(address.getStreet()).thenReturn("Rua Teste");

        Money money = mock(Money.class);
        when(money.getMoneyValue()).thenReturn(new BigDecimal("99.99"));

        Email email = mock(Email.class);
        when(email.getEmail()).thenReturn("john@email.com");

        User user = mock(User.class);
        when(user.getEmail()).thenReturn(email);

        Customer customer = mock(Customer.class);
        when(customer.getUser()).thenReturn(user);

        OrderId orderId = mock(OrderId.class);
        when(orderId.getId()).thenReturn(UUID.randomUUID());

        Order order = mock(Order.class);
        when(order.getCustomer()).thenReturn(customer);
        when(order.getId()).thenReturn(orderId);
        when(order.getTotalPrice()).thenReturn(money);
        when(order.getAddress()).thenReturn(address);

        User carrier = mock(User.class);

        UserId userId = mock(UserId.class);
        when(userId.getId()).thenReturn(userUuid);
        when(carrier.getId()).thenReturn(userId);

        when(orderRepository.findById(OrderId.fromString(orderIdUUID)))
                .thenReturn(Optional.of(order));
        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(carrier));
        when(carrier.getId()).thenReturn(userId);

        orderService.pickupOrder(orderIdUUID, supabaseUserId);


        verify(order).pickup(carrier);

        verify(orderRepository).save(order);


        verify(orderAuditLogger).logPickupAttempt(orderIdUUID, userUuid.toString());
        verify(orderAuditLogger).logPickupSuccess(orderIdUUID, userUuid.toString());
        verify(orderAuditLogger, never()).logPickupFailure(any(), any(), any());
    }

    @Test
    void shouldThrowAndLogFailureWhenOrderNotFound() {
        String supabaseUserId = UUID.randomUUID().toString();
        String orderIdUUID = UUID.randomUUID().toString();
        UUID userUuid = UUID.randomUUID();

        User user = mock(User.class);

        UserId userId = mock(UserId.class);
        when(userId.getId()).thenReturn(userUuid);
        when(user.getId()).thenReturn(userId);

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);

        when(orderRepository.findById(OrderId.fromString(orderIdUUID)))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.pickupOrder(orderIdUUID, supabaseUserId));

        assertEquals("Order not found", ex.getMessage());

        verify(orderAuditLogger).logPickupAttempt(orderIdUUID, userUuid.toString());
        verify(orderAuditLogger).logPickupFailure(eq(orderIdUUID), eq(userUuid.toString()), any());
        verify(orderAuditLogger, never()).logPickupSuccess(any(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowAndLogFailureWhenCarrierProfileNotFound() {
        String supabaseUserId = UUID.randomUUID().toString();
        String orderIdUUID = UUID.randomUUID().toString();
        UUID userUuid = UUID.randomUUID();

        User user = mock(User.class);

        UserId userId = mock(UserId.class);


        Order order = mock(Order.class);

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.pickupOrder(orderIdUUID, supabaseUserId));

        assertEquals("User not found", ex.getMessage());

        verify(order, never()).pickup(any());
        verify(orderRepository, never()).save(any());
    }
    @Test
    void shouldThrowAndLogFailureWhenDomainRuleViolated() {
        String supabaseUserId = UUID.randomUUID().toString();
        String orderIdUUID = UUID.randomUUID().toString();
        UUID userUuid = UUID.randomUUID();

        Order order = mock(Order.class);
        User carrier = mock(User.class);

        UserId userId = mock(UserId.class);
        when(userId.getId()).thenReturn(userUuid);
        when(carrier.getId()).thenReturn(userId);

        when(orderRepository.findById(OrderId.fromString(orderIdUUID)))
                .thenReturn(Optional.of(order));
        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(carrier));
        when(carrier.getId()).thenReturn(userId);

        // Simulate domain rejecting the transition (e.g. already PICKED_UP)
        doThrow(new BusinessException("Order cannot be picked up: current status is PICKED_UP"))
                .when(order).pickup(carrier);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.pickupOrder(orderIdUUID, supabaseUserId));

        assertTrue(ex.getMessage().contains("PICKED_UP"));

        verify(orderAuditLogger).logPickupAttempt(orderIdUUID, userUuid.toString());
        verify(orderAuditLogger).logPickupFailure(eq(orderIdUUID), eq(userUuid.toString()), any());
        verify(orderRepository, never()).save(any());
    }

    private CreateOrderRequestDTO mockCompleteCreateOrderRequest(UUID cartId) {
        AddAddressDTO address = mock(AddAddressDTO.class);
        when(address.postalCode()).thenReturn("4000-001");
        when(address.city()).thenReturn("Porto");
        when(address.country()).thenReturn("Portugal");
        when(address.street()).thenReturn("Rua Teste");

        CreateOrderRequestDTO request = mock(CreateOrderRequestDTO.class);
        when(request.cartID()).thenReturn(cartId.toString());
        when(request.address()).thenReturn(address);

        return request;
    }

    private CreateOrderRequestDTO mockCreateOrderRequestWithIdsOnly(UUID cartId) {
        CreateOrderRequestDTO request = mock(CreateOrderRequestDTO.class);
        when(request.cartID()).thenReturn(cartId.toString());

        return request;
    }

    private CreateOrderRequestDTO mockCreateOrderRequestWithCartIdOnly(UUID cartId) {
        CreateOrderRequestDTO request = mock(CreateOrderRequestDTO.class);
        when(request.cartID()).thenReturn(cartId.toString());

        return request;
    }
}
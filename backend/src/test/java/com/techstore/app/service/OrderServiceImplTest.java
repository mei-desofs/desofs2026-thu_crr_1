package com.techstore.app.service;

import com.techstore.app.config.FileUploadConfig;
import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartId;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.customer.CustomerId;
import com.techstore.app.domain.order.Order;
import com.techstore.app.domain.order.OrderId;
import com.techstore.app.domain.order.OrderItem;
import com.techstore.app.domain.order.OrderStatus;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Address;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.domain.user.User;
import com.techstore.app.domain.user.UserId;
import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.ManagerOrderResponseDTO;
import com.techstore.app.dto.order.OrderResponseDTO;
import com.techstore.app.dto.order.OrderSummaryDTO;
import com.techstore.app.dto.product.OrderFilterDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
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

    @Mock
    private FileUploadConfig fileUploadConfig;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void shouldCreateOrderAndReturnResponse() {
        String supabaseUserId = UUID.randomUUID().toString();

        UUID userUuid = UUID.randomUUID();
        UUID cartUuid = UUID.randomUUID();
        UUID customerUuid = UUID.randomUUID();

        CreateOrderRequestDTO request = createRequest();

        User user = mock(User.class);
        UserId userId = mock(UserId.class);

        Cart cart = mock(Cart.class);
        Customer customer = mock(Customer.class);

        Product product = mock(Product.class);
        OrderItem orderItem = new OrderItem(1, new BigDecimal("99.99"), product);
        List<OrderItem> orderItems = List.of(orderItem);

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(userId);
        when(userId.getId()).thenReturn(userUuid);
        when(user.getEmail()).thenReturn(new Email("john@example.com"));

        when(cartRepository.findBySupabaseUserId(UUID.fromString(supabaseUserId)))
                .thenReturn(Optional.of(cart));

        when(cart.getId()).thenReturn(CartId.fromString(cartUuid.toString()));
        when(cart.toOrderItems()).thenReturn(orderItems);
        when(cart.calculateTotal()).thenReturn(new BigDecimal("99.99"));

        when(customerRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(customer));

        when(customer.getId()).thenReturn(CustomerId.fromString(customerUuid.toString()));
        when(customer.getUser()).thenReturn(user);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = orderService.createOrder(request, supabaseUserId);

        assertNotNull(response);
        assertNotNull(response.orderID());
        assertEquals(customerUuid.toString(), response.customerID());
        assertEquals(cartUuid.toString(), response.cartID());

        assertEquals("4000-001", response.address().postalCode());
        assertEquals("Porto", response.address().city());
        assertEquals("Portugal", response.address().country());
        assertEquals("Rua Teste", response.address().street());

        verify(userRepository).findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId));
        verify(cartRepository).findBySupabaseUserId(UUID.fromString(supabaseUserId));
        verify(customerRepository).findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId));

        verify(orderAuditLogger).logOrderCreationAttempt(
                userUuid.toString(),
                cartUuid.toString()
        );

        verify(cart).toOrderItems();
        verify(cart).calculateTotal();

        verify(product).decreaseStock(orderItem.getQuantity());

        verify(orderRepository).save(any(Order.class));

        verify(cart).clearItems();
        verify(cartRepository).save(cart);

        verify(notificationService).sendEmail(
                eq("john@example.com"),
                contains("TechStore - Order Confirmation #"),
                contains("Order Confirmed")
        );

        verify(orderAuditLogger).logOrderCreationSuccess(
                eq(response.orderID()),
                eq(userUuid.toString()),
                eq(cartUuid.toString())
        );

        verify(orderAuditLogger, never()).logOrderCreationFailure(any(), any(), any());
    }

    @Test
    void shouldThrowWhenCartDoesNotExist() {
        String supabaseUserId = UUID.randomUUID().toString();

        UUID userUuid = UUID.randomUUID();

        CreateOrderRequestDTO request = createRequest();

        User user = mock(User.class);
        UserId userId = mock(UserId.class);

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(userId);
        when(userId.getId()).thenReturn(userUuid);

        when(cartRepository.findBySupabaseUserId(UUID.fromString(supabaseUserId)))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder(request, supabaseUserId)
        );

        assertEquals("Cart not found", exception.getMessage());

        verify(userRepository).findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId));

        verify(cartRepository, times(2)).findBySupabaseUserId(UUID.fromString(supabaseUserId));

        verify(customerRepository, never()).findBySupabaseUserId(any(SupabaseUserId.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartRepository, never()).save(any(Cart.class));
        verify(notificationService, never()).sendEmail(any(), any(), any());

        verify(orderAuditLogger, never()).logOrderCreationAttempt(any(), any());

        verify(orderAuditLogger).logOrderCreationFailure(
                eq(userUuid.toString()),
                isNull(),
                same(exception)
        );
    }

    @Test
    void shouldThrowWhenCustomerDoesNotExist() {
        String supabaseUserId = UUID.randomUUID().toString();

        UUID userUuid = UUID.randomUUID();
        UUID cartUuid = UUID.randomUUID();

        CreateOrderRequestDTO request = createRequest();

        User user = mock(User.class);
        UserId userId = mock(UserId.class);

        Cart cart = mock(Cart.class);

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(userId);
        when(userId.getId()).thenReturn(userUuid);

        when(cartRepository.findBySupabaseUserId(UUID.fromString(supabaseUserId)))
                .thenReturn(Optional.of(cart));

        when(cart.getId()).thenReturn(CartId.fromString(cartUuid.toString()));

        when(customerRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder(request, supabaseUserId)
        );

        assertEquals("Customer not found", exception.getMessage());

        verify(userRepository).findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId));

        verify(cartRepository, times(2)).findBySupabaseUserId(UUID.fromString(supabaseUserId));

        verify(customerRepository).findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId));

        verify(orderAuditLogger).logOrderCreationAttempt(
                userUuid.toString(),
                cartUuid.toString()
        );

        verify(cart, never()).toOrderItems();
        verify(cart, never()).calculateTotal();
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartRepository, never()).save(any(Cart.class));
        verify(notificationService, never()).sendEmail(any(), any(), any());

        verify(orderAuditLogger).logOrderCreationFailure(
                eq(userUuid.toString()),
                eq(cartUuid.toString()),
                same(exception)
        );
    }

    @Test
    void shouldCreateOrderEvenWhenNotificationServiceFails() {
        String supabaseUserId = UUID.randomUUID().toString();

        UUID userUuid = UUID.randomUUID();
        UUID cartUuid = UUID.randomUUID();
        UUID customerUuid = UUID.randomUUID();

        CreateOrderRequestDTO request = createRequest();

        User user = mock(User.class);
        UserId userId = mock(UserId.class);

        Cart cart = mock(Cart.class);
        Customer customer = mock(Customer.class);

        Product product = mock(Product.class);
        OrderItem orderItem = new OrderItem(1, new BigDecimal("99.99"), product);
        List<OrderItem> orderItems = List.of(orderItem);

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(userId);
        when(userId.getId()).thenReturn(userUuid);
        when(user.getEmail()).thenReturn(new Email("john@example.com"));

        when(cartRepository.findBySupabaseUserId(UUID.fromString(supabaseUserId)))
                .thenReturn(Optional.of(cart));

        when(cart.getId()).thenReturn(CartId.fromString(cartUuid.toString()));
        when(cart.toOrderItems()).thenReturn(orderItems);
        when(cart.calculateTotal()).thenReturn(new BigDecimal("99.99"));

        when(customerRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(customer));

        when(customer.getId()).thenReturn(CustomerId.fromString(customerUuid.toString()));
        when(customer.getUser()).thenReturn(user);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(new RuntimeException("Email failed"))
                .when(notificationService)
                .sendEmail(any(), any(), any());

        OrderResponseDTO response = orderService.createOrder(request, supabaseUserId);

        assertNotNull(response);
        assertNotNull(response.orderID());
        assertEquals(customerUuid.toString(), response.customerID());
        assertEquals(cartUuid.toString(), response.cartID());

        verify(orderRepository).save(any(Order.class));
        verify(cart).clearItems();
        verify(cartRepository).save(cart);
        verify(notificationService).sendEmail(any(), any(), any());

        verify(orderAuditLogger).logOrderCreationAttempt(
                userUuid.toString(),
                cartUuid.toString()
        );

        verify(orderAuditLogger).logOrderCreationSuccess(
                eq(response.orderID()),
                eq(userUuid.toString()),
                eq(cartUuid.toString())
        );

        verify(orderAuditLogger, never()).logOrderCreationFailure(any(), any(), any());
    }

    private CreateOrderRequestDTO createRequest() {
        return new CreateOrderRequestDTO(
                new AddAddressDTO(
                        "4000-001",
                        "Porto",
                        "Portugal",
                        "Rua Teste"
                )
        );
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

        doThrow(new BusinessException("Order cannot be picked up: current status is PICKED_UP"))
                .when(order).pickup(carrier);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.pickupOrder(orderIdUUID, supabaseUserId));

        assertTrue(ex.getMessage().contains("PICKED_UP"));

        verify(orderAuditLogger).logPickupAttempt(orderIdUUID, userUuid.toString());
        verify(orderAuditLogger).logPickupFailure(eq(orderIdUUID), eq(userUuid.toString()), any());
        verify(orderRepository, never()).save(any());
    }
    @Test
    void shouldReturnPendingOrders() {

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

        when(orderRepository.findByOrderStatus(OrderStatus.PENDING))
                .thenReturn(List.of(order1, order2));

        List<OrderSummaryDTO> response = orderService.getPendingOrders(supabaseUserId);

        assertEquals(2, response.size());

        verify(orderAuditLogger).logPendingOrdersListingAttempt(userUuid.toString());

        verify(orderAuditLogger).logPendingOrdersListingSuccess(userUuid.toString(), 2);
    }

    @Test
void shouldFindAllOrdersWithoutFilters() {
    String managerId = "manager-001";
 
    OrderFilterDTO filter = new OrderFilterDTO(null, null, null, null);
 
    Pageable pageable = PageRequest.of(0, 10);
 
    Order order1 = mock(Order.class);
    Order order2 = mock(Order.class);
 
    OrderId orderId1 = mock(OrderId.class);
    OrderId orderId2 = mock(OrderId.class);
 
    Customer customer1 = mock(Customer.class);
    Customer customer2 = mock(Customer.class);
 
    CustomerId customerId1 = mock(CustomerId.class);
    CustomerId customerId2 = mock(CustomerId.class);
 
    User user1 = mock(User.class);
    User user2 = mock(User.class);
 
    Email email1 = mock(Email.class);
    Email email2 = mock(Email.class);
 
    when(orderId1.getId()).thenReturn(UUID.randomUUID());
    when(orderId2.getId()).thenReturn(UUID.randomUUID());
 
    when(order1.getId()).thenReturn(orderId1);
    when(order2.getId()).thenReturn(orderId2);
 
    when(order1.getCustomer()).thenReturn(customer1);
    when(order2.getCustomer()).thenReturn(customer2);
 
    when(customer1.getId()).thenReturn(customerId1);
    when(customer2.getId()).thenReturn(customerId2);
 
    when(customer1.getUser()).thenReturn(user1);
    when(customer2.getUser()).thenReturn(user2);
 
    when(user1.getEmail()).thenReturn(email1);
    when(user2.getEmail()).thenReturn(email2);
 
    when(email1.getEmail()).thenReturn("customer1@example.com");
    when(email2.getEmail()).thenReturn("customer2@example.com");
 
    when(customerId1.getId()).thenReturn(UUID.randomUUID());
    when(customerId2.getId()).thenReturn(UUID.randomUUID());
 
    when(order1.getOrderStatus()).thenReturn(OrderStatus.PENDING);
    when(order2.getOrderStatus()).thenReturn(OrderStatus.PENDING);
 
    when(order1.getTotalPrice()).thenReturn(new Money(new BigDecimal("100.00")));
    when(order2.getTotalPrice()).thenReturn(new Money(new BigDecimal("200.00")));
 
    when(order1.getCreatedAt()).thenReturn(LocalDateTime.now());
    when(order2.getCreatedAt()).thenReturn(LocalDateTime.now());
 
    when(order1.getOrderItems()).thenReturn(new java.util.ArrayList<>());
    when(order2.getOrderItems()).thenReturn(new java.util.ArrayList<>());
 
    Page<Order> ordersPage = new PageImpl<>(List.of(order1, order2), pageable, 2);
 
    when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(ordersPage);
 
    Page<ManagerOrderResponseDTO> response = orderService.findAllOrders(filter, pageable, managerId);
 
    assertEquals(2, response.getTotalElements());
    assertEquals(1, response.getTotalPages());
    assertEquals(2, response.getContent().size());
 
    verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    verify(orderAuditLogger).logOrdersFiltered(managerId, null, null, 2);
}
 
@Test
void shouldFindAllOrdersWithStatusFilter() {
    String managerId = "manager-001";
 
    OrderFilterDTO filter = new OrderFilterDTO(OrderStatus.DELIVERED, null, null, null);
 
    Pageable pageable = PageRequest.of(0, 10);
 
    Order order = mock(Order.class);
    OrderId orderId = mock(OrderId.class);
    Customer customer = mock(Customer.class);
    CustomerId customerId = mock(CustomerId.class);
    User user = mock(User.class);
    Email email = mock(Email.class);
 
    when(orderId.getId()).thenReturn(UUID.randomUUID());
    when(order.getId()).thenReturn(orderId);
    when(order.getCustomer()).thenReturn(customer);
    when(customer.getId()).thenReturn(customerId);
    when(customerId.getId()).thenReturn(UUID.randomUUID());
    when(customer.getUser()).thenReturn(user);
    when(user.getEmail()).thenReturn(email);
    when(email.getEmail()).thenReturn("customer@example.com");
    when(order.getOrderStatus()).thenReturn(OrderStatus.DELIVERED);
    when(order.getTotalPrice()).thenReturn(new Money(new BigDecimal("150.00")));
    when(order.getCreatedAt()).thenReturn(LocalDateTime.now());
    when(order.getOrderItems()).thenReturn(new java.util.ArrayList<>());
 
    Page<Order> ordersPage = new PageImpl<>(List.of(order), pageable, 1);
 
    when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(ordersPage);
 
    Page<ManagerOrderResponseDTO> response = orderService.findAllOrders(filter, pageable, managerId);
 
    assertEquals(1, response.getTotalElements());
    assertEquals(1, response.getContent().size());
 
    verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    verify(orderAuditLogger).logOrdersFiltered(eq(managerId), eq("DELIVERED"), isNull(), eq(1));
}
 
@Test
void shouldFindAllOrdersWithEmailFilter() {
    String managerId = "manager-001";
    String customerEmail = "john@example.com";
 
    OrderFilterDTO filter = new OrderFilterDTO(null, customerEmail, null, null);
 
    Pageable pageable = PageRequest.of(0, 10);
 
    Order order = mock(Order.class);
    OrderId orderId = mock(OrderId.class);
    Customer customer = mock(Customer.class);
    CustomerId customerId = mock(CustomerId.class);
    User user = mock(User.class);
    Email email = mock(Email.class);
 
    when(orderId.getId()).thenReturn(UUID.randomUUID());
    when(order.getId()).thenReturn(orderId);
    when(order.getCustomer()).thenReturn(customer);
    when(customer.getId()).thenReturn(customerId);
    when(customerId.getId()).thenReturn(UUID.randomUUID());
    when(customer.getUser()).thenReturn(user);
    when(user.getEmail()).thenReturn(email);
    when(email.getEmail()).thenReturn(customerEmail);
    when(order.getOrderStatus()).thenReturn(OrderStatus.PENDING);
    when(order.getTotalPrice()).thenReturn(new Money(new BigDecimal("75.00")));
    when(order.getCreatedAt()).thenReturn(LocalDateTime.now());
    when(order.getOrderItems()).thenReturn(new java.util.ArrayList<>());
 
    Page<Order> ordersPage = new PageImpl<>(List.of(order), pageable, 1);
 
    when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(ordersPage);
 
    Page<ManagerOrderResponseDTO> response = orderService.findAllOrders(filter, pageable, managerId);
 
    assertEquals(1, response.getTotalElements());
 
    verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    verify(orderAuditLogger).logOrdersFiltered(eq(managerId), isNull(), eq(customerEmail), eq(1));
}
 
@Test
void shouldReturnEmptyPageWhenNoOrdersMatch() {
    String managerId = "manager-001";
 
    OrderFilterDTO filter = new OrderFilterDTO(null, null, null, null);
 
    Pageable pageable = PageRequest.of(0, 10);
 
    Page<Order> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
 
    when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);
 
    Page<ManagerOrderResponseDTO> response = orderService.findAllOrders(filter, pageable, managerId);
 
    assertEquals(0, response.getTotalElements());
    assertEquals(0, response.getContent().size());
 
    verify(orderAuditLogger).logOrdersFiltered(eq(managerId), isNull(), isNull(), eq(0));
}
 
@Test
void shouldHandleExceptionWhenFindingOrders() {
    String managerId = "manager-001";
 
    OrderFilterDTO filter = new OrderFilterDTO(null, null, null, null);
 
    Pageable pageable = PageRequest.of(0, 10);
 
    when(orderRepository.findAll(any(Specification.class), eq(pageable)))
            .thenThrow(new RuntimeException("Database error"));
 
    RuntimeException exception = assertThrows(RuntimeException.class,
            () -> orderService.findAllOrders(filter, pageable, managerId));
 
    assertEquals("Database error", exception.getMessage());
 
    verify(orderAuditLogger).logOrdersAccessFailure(eq(managerId), contains("Database error"));
}
 
@Test
void shouldFindOrdersWithPagination() {
    String managerId = "manager-001";
 
    OrderFilterDTO filter = new OrderFilterDTO(null, null, null, null);
 
    Pageable pageable = PageRequest.of(1, 5);
 
    Order order1 = mock(Order.class);
    Order order2 = mock(Order.class);
 
    OrderId orderId1 = mock(OrderId.class);
    OrderId orderId2 = mock(OrderId.class);
 
    Customer customer1 = mock(Customer.class);
    Customer customer2 = mock(Customer.class);
 
    CustomerId customerId1 = mock(CustomerId.class);
    CustomerId customerId2 = mock(CustomerId.class);
 
    User user1 = mock(User.class);
    User user2 = mock(User.class);
 
    Email email1 = mock(Email.class);
    Email email2 = mock(Email.class);
 
    when(orderId1.getId()).thenReturn(UUID.randomUUID());
    when(orderId2.getId()).thenReturn(UUID.randomUUID());
 
    when(order1.getId()).thenReturn(orderId1);
    when(order2.getId()).thenReturn(orderId2);
 
    when(order1.getCustomer()).thenReturn(customer1);
    when(order2.getCustomer()).thenReturn(customer2);
 
    when(customer1.getId()).thenReturn(customerId1);
    when(customer2.getId()).thenReturn(customerId2);
 
    when(customer1.getUser()).thenReturn(user1);
    when(customer2.getUser()).thenReturn(user2);
 
    when(user1.getEmail()).thenReturn(email1);
    when(user2.getEmail()).thenReturn(email2);
 
    when(email1.getEmail()).thenReturn("customer1@example.com");
    when(email2.getEmail()).thenReturn("customer2@example.com");
 
    when(customerId1.getId()).thenReturn(UUID.randomUUID());
    when(customerId2.getId()).thenReturn(UUID.randomUUID());
 
    when(order1.getOrderStatus()).thenReturn(OrderStatus.PENDING);
    when(order2.getOrderStatus()).thenReturn(OrderStatus.PENDING);
 
    when(order1.getTotalPrice()).thenReturn(new Money(new BigDecimal("100.00")));
    when(order2.getTotalPrice()).thenReturn(new Money(new BigDecimal("120.00")));
 
    when(order1.getCreatedAt()).thenReturn(LocalDateTime.now());
    when(order2.getCreatedAt()).thenReturn(LocalDateTime.now());
 
    when(order1.getOrderItems()).thenReturn(new java.util.ArrayList<>());
    when(order2.getOrderItems()).thenReturn(new java.util.ArrayList<>());
 
    Page<Order> ordersPage = new PageImpl<>(List.of(order1, order2), pageable, 12);
 
    when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(ordersPage);
 
    Page<ManagerOrderResponseDTO> response = orderService.findAllOrders(filter, pageable, managerId);
 
    assertEquals(12, response.getTotalElements());
    assertEquals(3, response.getTotalPages());
    assertEquals(1, response.getNumber());
    assertEquals(2, response.getContent().size());
 
    verify(orderRepository).findAll(any(Specification.class), eq(pageable));
}
 
@Test
void shouldFindOrdersWithMultipleFilters() {
    String managerId = "manager-001";
    String customerEmail = "jane@example.com";
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 12, 31);
 
    OrderFilterDTO filter = new OrderFilterDTO(OrderStatus.SHIPPED, customerEmail, startDate, endDate);
 
    Pageable pageable = PageRequest.of(0, 10);
 
    Order order = mock(Order.class);
    OrderId orderId = mock(OrderId.class);
    Customer customer = mock(Customer.class);
    CustomerId customerId = mock(CustomerId.class);
    User user = mock(User.class);
    Email email = mock(Email.class);
 
    when(orderId.getId()).thenReturn(UUID.randomUUID());
    when(order.getId()).thenReturn(orderId);
    when(order.getCustomer()).thenReturn(customer);
    when(customer.getId()).thenReturn(customerId);
    when(customerId.getId()).thenReturn(UUID.randomUUID());
    when(customer.getUser()).thenReturn(user);
    when(user.getEmail()).thenReturn(email);
    when(email.getEmail()).thenReturn(customerEmail);
    when(order.getOrderStatus()).thenReturn(OrderStatus.SHIPPED);
    when(order.getTotalPrice()).thenReturn(new Money(new BigDecimal("250.00")));
    when(order.getCreatedAt()).thenReturn(LocalDateTime.now());
    when(order.getOrderItems()).thenReturn(new java.util.ArrayList<>());
 
    Page<Order> ordersPage = new PageImpl<>(List.of(order), pageable, 1);
 
    when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(ordersPage);
 
    Page<ManagerOrderResponseDTO> response = orderService.findAllOrders(filter, pageable, managerId);
 
    assertEquals(1, response.getTotalElements());
 
    verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    verify(orderAuditLogger).logOrdersFiltered(
            eq(managerId),
            eq("SHIPPED"),
            eq(customerEmail),
            eq(1)
    );

    }
}
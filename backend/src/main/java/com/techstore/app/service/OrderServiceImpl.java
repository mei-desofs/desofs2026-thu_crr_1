package com.techstore.app.service;

import java.math.BigDecimal;
import java.util.List;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartId;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.customer.CustomerId;
import com.techstore.app.domain.user.User;
import com.techstore.app.domain.user.UserId;
import com.techstore.app.domain.order.OrderId;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.OrderResponseDTO;
import com.techstore.app.dto.order.OrderSummaryDTO;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.logger.OrderAuditLogger;
import com.techstore.app.mapper.OrderMapper;
import com.techstore.app.domain.order.Order;
import com.techstore.app.domain.order.OrderItem;
import com.techstore.app.repository.*;
import com.techstore.app.service.interfaces.NotificationService;
import com.techstore.app.service.interfaces.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final CartRepository cartRepository;

    private final CustomerRepository customerRepository;

    private final UserRepository userRepository;

    private final OrderAuditLogger orderAuditLogger;

    private final NotificationService notificationService;

    public OrderServiceImpl(OrderRepository orderRepository, CartRepository cartRepository,
            CustomerRepository customerRepository, UserRepository userRepository, OrderAuditLogger orderAuditLogger,
            NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.orderAuditLogger = orderAuditLogger;
        this.notificationService = notificationService;
    }

    @Transactional
    @Override
    public OrderResponseDTO createOrder(CreateOrderRequestDTO request, String supabaseUserId) {

        String userId = userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)).
                orElseThrow(()-> new BusinessException("User not found")).getId().getId().toString();

        orderAuditLogger.logOrderCreationAttempt(request, userId);

        try {
            Cart cart = cartRepository.findById(CartId.fromString(request.cartID()))
                    .orElseThrow(() -> new BusinessException("Cart not found"));

            Customer customer = customerRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId))
                    .orElseThrow(() -> new BusinessException("Customer not found"));

            List<OrderItem> orderItems = cart.toOrderItems();
            BigDecimal total = cart.calculateTotal();

            Order order = OrderMapper.toEntity(request, orderItems, total);
            order.setCustomer(customer);

            Order saved = orderRepository.save(order);

            cart.clearItems();
            cartRepository.save(cart);

            final String emailBody = createOrderEmailBody(customer, saved);
            notificationService.sendOrderConfirmationEmail(customer.getUser().getEmail().getEmail(),
                    "TechStore - Order Confirmation #" + saved.getId().getId(), emailBody);

            OrderResponseDTO response = OrderMapper.toResponse(saved, request.cartID());

            orderAuditLogger.logOrderCreation(response.orderID(), userId, response.cartID());

            return response;

        } catch (RuntimeException exception) {
            orderAuditLogger.logOrderCreationFailure(request, userId, exception);
            throw exception;
        }
    }

    private static String createOrderEmailBody(Customer customer, Order saved) {
        // Send order confirmation email
        return """
                    <h3>Order Confirmed 🎉</h3>

                    <p>Hi %s,</p>

                    <p>Your order has been successfully created.</p>

                    <p>
                        <b>Order ID:</b> %s<br/>
                        <b>Total:</b> %s€
                    </p>

                    <p>We are processing your order and will update you soon.</p>

                    <br/>

                    <p>Thank you for shopping with TechStore.</p>
                """.formatted(
                customer.getUser().getEmail().getEmail().split("@")[0], // Use the part before @ as the name
                saved.getId().getId(),
                saved.getTotalPrice().getMoneyValue());
    }

    @Transactional
    @Override
    public List<OrderSummaryDTO> getOrdersByCustomer(String supabaseUserId) {

        String userId = userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)).
                orElseThrow(()-> new BusinessException("User not found")).getId().getId().toString();

        orderAuditLogger.logCustomerOrdersListingAttempt(userId);

        try {
            Customer customer = customerRepository
                    .findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId))
                    .orElseThrow(() -> new BusinessException("Customer not found"));

            List<Order> orders = orderRepository.findByCustomer(customer);

            List<OrderSummaryDTO> response =
                    orders.stream()
                            .map(order -> OrderMapper.toSummary(order))
                            .toList();

            orderAuditLogger.logCustomerOrdersListingSuccess(userId, response.size());

            return response;

        } catch (RuntimeException exception) {
            orderAuditLogger.logCustomerOrdersListingFailure(userId, exception);
            throw exception;
        }

    }
    @Transactional
    @Override
    public List<OrderSummaryDTO> getOrdersByCarrier(String supabaseUserId) {

        String userId = userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)).
                orElseThrow(()-> new BusinessException("User not found")).getId().getId().toString();

        orderAuditLogger.logCarrierOrdersListingAttempt(userId);

        try {
            User carrier = userRepository
                    .findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId))
                    .orElseThrow(() -> new BusinessException("User not found"));

            List<Order> orders = orderRepository.findByCarrier(carrier);

            List<OrderSummaryDTO> response =
                    orders.stream()
                            .map(order -> OrderMapper.toSummary(order))
                            .toList();

            orderAuditLogger.logCarrierOrdersListingSuccess(userId, response.size());

            return response;

        } catch (RuntimeException exception) {
            orderAuditLogger.logCarrierOrdersListingFailure(userId, exception);
            throw exception;
        }

    }

    @Transactional
    @Override
    public void pickupOrder(String orderId, String supabaseUserId) {

        User carrier = userRepository
                .findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId))
                .orElseThrow(() -> new BusinessException("User not found"));

        String userId = carrier.getId().getId().toString();

        orderAuditLogger.logPickupAttempt(orderId, userId);

        try {
            Order order = orderRepository.findById(OrderId.fromString(orderId))
                    .orElseThrow(() -> new BusinessException("Order not found"));

            order.pickup(carrier);

            orderRepository.save(order);

            String customerEmail = order.getCustomer().getUser().getEmail().getEmail();
            String subject = "TechStore - Your order #" + order.getId().getId() + " has been picked up";
            String body = createPickupEmailBody(order);
            notificationService.sendOrderConfirmationEmail(customerEmail, subject, body);

            orderAuditLogger.logPickupSuccess(orderId, userId);

        } catch (RuntimeException ex) {
            orderAuditLogger.logPickupFailure(orderId, userId, ex);
            throw ex;
        }
    }
    private static String createPickupEmailBody(Order order) {
        return """
                <h3>Your order is on the way! 🚚</h3>

                <p>Good news! Your order has been picked up by a carrier and is now on its way to you.</p>

                <p>
                    <b>Order ID:</b> %s<br/>
                    <b>Total:</b> %s€<br/>
                    <b>Delivery address:</b> %s, %s, %s
                </p>

                <p>You will be notified once your order has been delivered.</p>

                <br/>

                <p>Thank you for shopping with TechStore.</p>
            """.formatted(
                order.getId().getId(),
                order.getTotalPrice().getMoneyValue(),
                order.getAddress().getStreet(),
                order.getAddress().getCity(),
                order.getAddress().getCountry());
    }
}

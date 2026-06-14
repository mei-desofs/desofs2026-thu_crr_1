package com.techstore.app.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.techstore.app.config.FileUploadConfig;
import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.order.OrderStatus;
import com.techstore.app.domain.user.User;
import com.techstore.app.domain.order.OrderId;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.ManagerOrderResponseDTO;
import com.techstore.app.dto.order.OrderResponseDTO;
import com.techstore.app.dto.order.OrderSummaryDTO;
import com.techstore.app.dto.product.OrderFilterDTO;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.logger.OrderAuditLogger;
import com.techstore.app.mapper.OrderMapper;
import com.techstore.app.domain.order.Order;
import com.techstore.app.domain.order.OrderItem;
import com.techstore.app.repository.*;
import com.techstore.app.repository.Custom.OrderSpecification;
import com.techstore.app.service.interfaces.NotificationService;
import com.techstore.app.service.interfaces.OrderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
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

    private final FileUploadConfig fileUploadConfig;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    public OrderServiceImpl(OrderRepository orderRepository, CartRepository cartRepository,
            CustomerRepository customerRepository, UserRepository userRepository, OrderAuditLogger orderAuditLogger,
            NotificationService notificationService, FileUploadConfig fileUploadConfig) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.orderAuditLogger = orderAuditLogger;
        this.notificationService = notificationService;
        this.fileUploadConfig = fileUploadConfig;
    }

    @Transactional
    @Override
    public OrderResponseDTO createOrder(CreateOrderRequestDTO request, String supabaseUserId) {

        String userId = userRepository.findBySupabaseUserId(
                SupabaseUserId.fromString(supabaseUserId))
                .orElseThrow(() -> new BusinessException("User not found")).getId().getId().toString();

        try {
            Cart cart = cartRepository.findBySupabaseUserId(UUID.fromString(supabaseUserId))
                    .orElseThrow(() -> new BusinessException("Cart not found"));

            String cartId = cart.getId().getId().toString();

            orderAuditLogger.logOrderCreationAttempt(userId, cartId);

            Customer customer = customerRepository.findBySupabaseUserId(
                    SupabaseUserId.fromString(supabaseUserId))
                    .orElseThrow(() -> new BusinessException("Customer not found"));

            List<OrderItem> orderItems = cart.toOrderItems();
            BigDecimal total = cart.calculateTotal();

            // Stock validation + decrease
            orderItems.forEach(item -> item.getProduct().decreaseStock(item.getQuantity()));

            Order order = OrderMapper.toEntity(request, orderItems, total);
            order.setCustomer(customer);

            Order saved = orderRepository.save(order);

            cart.clearItems();
            cartRepository.save(cart);

            String customerEmail = customer.getUser().getEmail().getEmail();
            String emailBody = createOrderEmailBody(customer, saved);

            try {
                notificationService.sendEmail(customerEmail, "TechStore - Order Confirmation #"
                        + saved.getId().getId(), emailBody);
            } catch (Exception exception) {
                LOGGER.warn("Failed to send confirmation email for order {}", saved.getId().getId(), exception);
            }

            OrderResponseDTO response = OrderMapper.toResponse(saved, cartId);

            orderAuditLogger.logOrderCreationSuccess(saved.getId().getId().toString(), userId, cartId);

            return response;

        } catch (RuntimeException exception) {

            Cart fallbackCart = cartRepository.findBySupabaseUserId(UUID.fromString(supabaseUserId)).orElse(null);
            String cartId = fallbackCart != null ? fallbackCart.getId().getId().toString() : null;

            orderAuditLogger.logOrderCreationFailure(userId, cartId, exception);

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

        String userId = userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId))
                .orElseThrow(() -> new BusinessException("User not found")).getId().getId().toString();

        orderAuditLogger.logCustomerOrdersListingAttempt(userId);

        try {
            Customer customer = customerRepository
                    .findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId))
                    .orElseThrow(() -> new BusinessException("Customer not found"));

            List<Order> orders = orderRepository.findByCustomer(customer);

            List<OrderSummaryDTO> response = orders.stream()
                    .map(order -> OrderMapper.toSummary(order, fileUploadConfig.getBasePath()))
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

        String userId = userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId))
                .orElseThrow(() -> new BusinessException("User not found")).getId().getId().toString();

        orderAuditLogger.logCarrierOrdersListingAttempt(userId);

        try {
            User carrier = userRepository
                    .findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId))
                    .orElseThrow(() -> new BusinessException("User not found"));

            List<Order> orders = orderRepository.findByCarrier(carrier);

            List<OrderSummaryDTO> response = orders.stream()
                    .map(order -> OrderMapper.toSummary(order, fileUploadConfig.getBasePath()))
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
            notificationService.sendEmail(customerEmail, subject, body);

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

    @Transactional
    @Override
    public List<OrderSummaryDTO> getPendingOrders(String supabaseUserId) {

        String userId = userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId))
                .orElseThrow(() -> new BusinessException("User not found")).getId().getId().toString();

        orderAuditLogger.logPendingOrdersListingAttempt(userId);

        try {
            List<Order> orders = orderRepository.findByOrderStatus(OrderStatus.PENDING);

            List<OrderSummaryDTO> response = orders.stream()
                    .map(order -> OrderMapper.toSummary(order, fileUploadConfig.getBasePath()))
                    .toList();

            orderAuditLogger.logPendingOrdersListingSuccess(userId, response.size());

            return response;

        } catch (RuntimeException exception) {
            orderAuditLogger.logPendingOrdersListingFailure(userId, exception);
            throw exception;
        }

    }

    public Page<ManagerOrderResponseDTO> findAllOrders(
        OrderFilterDTO filter,
        Pageable pageable,
        String managerId) {
    try {
        Specification<Order> spec = OrderSpecification.withFilters(
            filter.status(),
            filter.customerEmail(),
            filter.startDate(),
            filter.endDate()
        );

        Page<Order> orders = orderRepository.findAll(spec, pageable);
        Page<ManagerOrderResponseDTO> result = orders.map(OrderMapper::toManagerResponse);

        orderAuditLogger.logOrdersFiltered(managerId,
            filter.status() != null ? filter.status().name() : null,
            filter.customerEmail(), (int) orders.getTotalElements());

        return result;
    } catch (Exception ex) {
        orderAuditLogger.logOrdersAccessFailure(managerId, ex.getMessage());
        throw ex;
    }
}

}

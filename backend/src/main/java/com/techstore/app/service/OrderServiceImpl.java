package com.techstore.app.service;

import java.math.BigDecimal;
import java.util.List;

import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.cart.CartId;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.customer.CustomerId;
import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.OrderResponseDTO;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.logger.OrderAuditLogger;
import com.techstore.app.mapper.OrderMapper;
import com.techstore.app.domain.order.Order;
import com.techstore.app.domain.order.OrderItem;
import com.techstore.app.repository.CartRepository;
import com.techstore.app.repository.CustomerRepository;
import com.techstore.app.repository.OrderRepository;
import com.techstore.app.service.interfaces.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final CartRepository cartRepository;

    private final CustomerRepository customerRepository;

    private final OrderAuditLogger orderAuditLogger;

    public OrderServiceImpl(OrderRepository orderRepository, CartRepository cartRepository,
            CustomerRepository customerRepository, OrderAuditLogger orderAuditLogger) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
        this.orderAuditLogger = orderAuditLogger;
    }

    @Override
    public OrderResponseDTO createOrder(CreateOrderRequestDTO request) {
        orderAuditLogger.logOrderCreationAttempt(request);

        try {
            Cart cart = cartRepository.findById(CartId.fromString(request.cartID()))
                    .orElseThrow(() -> new BusinessException("Cart not found"));

            Customer customer = customerRepository.findById(CustomerId.fromString(request.customerID()))
                    .orElseThrow(() -> new BusinessException("Customer not found"));

            List<OrderItem> orderItems = cart.toOrderItems();
            BigDecimal total = cart.calculateTotal();

            Order order = OrderMapper.toEntity(request, orderItems, total);
            order.setCustomer(customer);

            Order saved = orderRepository.save(order);

            OrderResponseDTO response = OrderMapper.toResponse(saved, request.cartID());

            orderAuditLogger.logOrderCreation(response.orderID(), response.customerID(), response.cartID());

            return response;

        } catch (RuntimeException exception) {
            orderAuditLogger.logOrderCreationFailure(request, exception);
            throw exception;
        }
    }
}

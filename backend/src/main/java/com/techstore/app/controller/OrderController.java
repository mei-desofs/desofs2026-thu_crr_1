package com.techstore.app.controller;

import com.techstore.app.dto.order.OrderSummaryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import com.techstore.app.config.ratelimit.annotation.RateLimit;
import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.OrderResponseDTO;
import com.techstore.app.service.interfaces.OrderService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @RateLimit("create-order")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody @Valid CreateOrderRequestDTO request, Authentication authentication) {
        String supabaseUserId = authentication.getName();

        return ResponseEntity.ok(orderService.createOrder(request,supabaseUserId));
    }
    @RateLimit("customer-list-orders")
    @GetMapping
    public ResponseEntity<List<OrderSummaryDTO>> getCustomerOrders(
            Authentication authentication) {

        String supabaseUserId = authentication.getName();
        List<OrderSummaryDTO> orders = orderService.getOrdersByCustomer(supabaseUserId);
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(orders);
    }
    @RateLimit("carrier-list-orders")
    @GetMapping("/carrier")
    public ResponseEntity<List<OrderSummaryDTO>> getCarrierOrders(
            Authentication authentication) {

        String supabaseUserId = authentication.getName();
        List<OrderSummaryDTO> orders = orderService.getOrdersByCarrier(supabaseUserId);
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(orders);
    }

    @RateLimit("carrier-pickup-order")
    @PatchMapping("/{orderId}/pickup")
    public ResponseEntity<Void> pickupOrder(
            @PathVariable String orderId,
            Authentication authentication) {

        String supabaseUserId = authentication.getName();

        orderService.pickupOrder(orderId, supabaseUserId);

        return ResponseEntity.noContent().build();
    }
    @RateLimit("carrier-list-pending-orders")
    @GetMapping("/pending")
    public ResponseEntity<List<OrderSummaryDTO>> getPendingOrders(
            Authentication authentication) {

        String supabaseUserId = authentication.getName();
        List<OrderSummaryDTO> orders = orderService.getPendingOrders(supabaseUserId);
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(orders);
    }
}

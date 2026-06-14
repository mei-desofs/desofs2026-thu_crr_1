package com.techstore.app.controller;

import com.techstore.app.dto.order.OrderSummaryDTO;
import com.techstore.app.dto.product.OrderFilterDTO;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable; 
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.techstore.app.config.ratelimit.annotation.RateLimit;
import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.ManagerOrderResponseDTO;
import com.techstore.app.dto.order.OrderResponseDTO;
import com.techstore.app.service.interfaces.OrderService;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @RateLimit("create-order")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody @Valid CreateOrderRequestDTO request,
            Authentication authentication) {
        String supabaseUserId = authentication.getName();

        return ResponseEntity.ok(orderService.createOrder(request, supabaseUserId));
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

    @GetMapping("/manager")
    @RateLimit("list-all-orders")
    public Page<ManagerOrderResponseDTO> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        Authentication authentication) {
        String managerId = authentication.getName();

        OrderFilterDTO filter = new OrderFilterDTO(status, customerEmail, startDate, endDate);
        return orderService.findAllOrders(filter, pageable, managerId);
    }
}

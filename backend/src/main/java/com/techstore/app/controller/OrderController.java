package com.techstore.app.controller;

import com.techstore.app.dto.order.OrderSummaryDTO;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody @Valid CreateOrderRequestDTO request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }
    @RateLimit("customer-list-orders")
    @GetMapping
    public ResponseEntity<List<OrderSummaryDTO>> getCustomerOrders(
            @RequestParam String customerId) {

        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }
    @RateLimit("carrier-list-orders")
    @GetMapping("/carrier")
    public ResponseEntity<List<OrderSummaryDTO>> getCarrierOrders(
            @RequestParam String carrierId) {

        return ResponseEntity.ok(orderService.getOrdersByCarrier(carrierId));
    }
}

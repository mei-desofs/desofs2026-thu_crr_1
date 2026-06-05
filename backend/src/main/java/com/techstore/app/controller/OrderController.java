package com.techstore.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techstore.app.config.ratelimit.annotation.RateLimit;
import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.OrderResponseDTO;
import com.techstore.app.service.interfaces.OrderService;

import jakarta.validation.Valid;

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
}

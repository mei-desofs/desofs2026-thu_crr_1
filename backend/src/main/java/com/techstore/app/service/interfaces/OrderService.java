package com.techstore.app.service.interfaces;

import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.OrderResponseDTO;

public interface OrderService {

    /**
     * Creates an order based on the provided request data.
     *
     * @param request The details required to create an order.
     * @return The created order's details.
     */
    public OrderResponseDTO createOrder(CreateOrderRequestDTO request);
}

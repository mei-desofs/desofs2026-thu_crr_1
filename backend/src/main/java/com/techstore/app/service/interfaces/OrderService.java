package com.techstore.app.service.interfaces;

import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.OrderResponseDTO;
import com.techstore.app.dto.order.OrderSummaryDTO;

import java.util.List;

public interface OrderService {

    /**
     * Creates an order based on the provided request data.
     *
     * @param request The details required to create an order.
     * @return The created order's details.
     */
    public OrderResponseDTO createOrder(CreateOrderRequestDTO request);

    /**
     * Retrieves a list of orders for a specific customer.
     *
     * @param customerId The ID of the customer whose orders are to be retrieved.
     * @return A list of orders associated with the specified customer.
     */
    public List<OrderSummaryDTO> getOrdersByCustomer(String customerId);

    /**
     * Retrieves a list of orders for a specific carrier.
     *
     * @param carrierId The ID of the carrier whose orders are to be retrieved.
     * @return A list of orders associated with the specified carrier.
     */
    List<OrderSummaryDTO> getOrdersByCarrier(String carrierId);
}

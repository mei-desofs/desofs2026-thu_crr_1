package com.techstore.app.service.interfaces;

import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.order.ManagerOrderResponseDTO;
import com.techstore.app.dto.order.OrderResponseDTO;
import com.techstore.app.dto.order.OrderSummaryDTO;
import com.techstore.app.dto.product.OrderFilterDTO;

import java.util.List;

import org.springframework.data.domain.Pageable; 
import org.springframework.data.domain.Page;

public interface OrderService {

    /**
     * Creates an order based on the provided request data.
     *
     * @param request The details required to create an order.
     * @return The created order's details.
     */
    public OrderResponseDTO createOrder(CreateOrderRequestDTO request, String supabaseUserId);

    /**
     * Retrieves a list of orders for a specific customer.
     *
     * @param supabaseUserId The Supabase user ID of the customer whose orders are to be retrieved.
     * @return A list of orders associated with the specified customer.
     */
    public List<OrderSummaryDTO> getOrdersByCustomer(String supabaseUserId);

    /**
     * Retrieves a list of orders for a specific carrier.
     *
     * @param carrierId The ID of the carrier whose orders are to be retrieved.
     * @return A list of orders associated with the specified carrier.
     */
    List<OrderSummaryDTO> getOrdersByCarrier(String carrierId);

    void pickupOrder(String orderId, String carrierId);

    public List<OrderSummaryDTO> getPendingOrders(String supabaseUserId);

    Page<ManagerOrderResponseDTO> findAllOrders(OrderFilterDTO filter, Pageable pageable,String managerId);

}

package com.techstore.app.dto.order;

import com.techstore.app.domain.order.OrderStatus;
import com.techstore.app.dto.shared.AddressDTO;

import java.math.BigDecimal;
import java.util.List;

public record OrderSummaryDTO(
    String orderId,
    String customerId,
    OrderStatus status,
    BigDecimal totalPrice,
    AddressDTO address,
    List<OrderItemDTO> items
) {
}

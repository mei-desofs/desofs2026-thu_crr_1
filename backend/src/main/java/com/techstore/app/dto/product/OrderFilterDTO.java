package com.techstore.app.dto.product;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.techstore.app.domain.order.OrderStatus;

public record OrderFilterDTO(
    OrderStatus status,
    String customerEmail,
    LocalDate startDate,
    LocalDate endDate
) {}

package com.techstore.app.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponseDTO(
    UUID id,
    UUID customerId,
    String customerEmail,
    BigDecimal totalPrice,
    String status,
    LocalDateTime createdAt,
    Integer itemCount
) {}
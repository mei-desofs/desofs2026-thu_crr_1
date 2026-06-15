package com.techstore.app.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techstore.app.domain.shared.Money;

public record ManagerOrderResponseDTO(
   UUID id, UUID id2, String email, Money totalPrice, String string,
            LocalDateTime createdAt, int size
) {}
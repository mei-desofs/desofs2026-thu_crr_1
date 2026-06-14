package com.techstore.app.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techstore.app.domain.shared.Money;

public record ManagerOrderResponseDTO(
   UUID id2, UUID id3, String email, Money totalPrice2, String string,
            LocalDateTime createdAt2, int size
) {}
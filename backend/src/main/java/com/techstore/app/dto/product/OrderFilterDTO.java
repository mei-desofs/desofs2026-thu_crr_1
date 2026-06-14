package com.techstore.app.dto.product;

import java.time.LocalDateTime;

public record OrderFilterDTO(
    String status,
    String customerEmail,
    LocalDateTime startDate,
    LocalDateTime endDate
) {}

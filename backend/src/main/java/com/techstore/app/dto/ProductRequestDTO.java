package com.techstore.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequestDTO (
        String name,
        String description,
        BigDecimal price,
        UUID categoryId
) {}
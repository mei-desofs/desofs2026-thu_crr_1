package com.techstore.app.dto;

import java.math.BigDecimal;

public record ProductRequestDTO (
        String name,
        String description,
        BigDecimal price,
        Long categoryId
) {}
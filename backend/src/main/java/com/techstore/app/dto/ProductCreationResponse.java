package com.techstore.app.dto;

import java.math.BigDecimal;

public record ProductCreationResponse (
        Long id,
        String name,
        String description,
        BigDecimal price,
        String categoryName
) {}

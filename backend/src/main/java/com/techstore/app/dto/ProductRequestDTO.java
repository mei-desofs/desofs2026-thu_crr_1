package com.techstore.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequestDTO (
        @NotBlank(message = "Product name cannot be blank")
        @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Product description cannot be blank")
        @Size(min = 10, max = 1000, message = "Product description must be between 10 and 1000 characters")
        String description,

        @NotNull(message = "Product price cannot be null")
        @DecimalMin(value = "0.00", message = "Product price must be a positive value")
        @DecimalMax(value = "999999.99", message = "Product price must be less than or equal to 999,999.99")
        BigDecimal price,

        @NotNull(message = "Category cannot be null")
        UUID categoryId
) {}
package com.techstore.app.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpdateDTO(
        @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters") String name,

        @Size(min = 10, max = 1000, message = "Product description must be between 10 and 1000 characters") String description,

        @DecimalMin(value = "0.00", inclusive = false, message = "Product price must be a positive value") @DecimalMax(value = "999999.99", message = "Product price must be less than or equal to 999,999.99") BigDecimal price,

        @DecimalMin(value = "0", message = "Product stock quantity must be zero or a positive value") @DecimalMax(value = "999999", message = "Product stock quantity must be less than or equal to 999,999") Integer stockQuantity,

        UUID categoryId) {
}

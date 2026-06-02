package com.techstore.app.dto.cart;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CartItemDto (
        @NotNull(message = "Product cannot be null")
        UUID productId,

        @NotNull(message = "Quantity cannot be null")
        @DecimalMin(value = "1", message = "Quantity must be at least 1")
        @DecimalMax(value = "999999", message = "Quantity must be less than or equal to 999,999")
        Integer quantity
) {}

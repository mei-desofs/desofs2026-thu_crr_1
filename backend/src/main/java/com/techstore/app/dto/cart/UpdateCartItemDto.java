package com.techstore.app.dto.cart;

import jakarta.validation.constraints.AssertTrue;

public record UpdateCartItemDto(
        Integer quantityDelta
) {
    @AssertTrue(message = "quantityDelta must be either -1 or 1")
    public boolean isValidDelta() {
        return quantityDelta != null &&
               (quantityDelta == -1 || quantityDelta == 1);
    }
}
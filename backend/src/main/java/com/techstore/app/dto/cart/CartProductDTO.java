package com.techstore.app.dto.cart;

import java.math.BigDecimal;

public record CartProductDTO(
        String productName,
        Integer quantity,
        BigDecimal unitPrice
) {
}
package com.techstore.app.dto.order;

import java.math.BigDecimal;

public record OrderItemDTO(
                String productId,
                String productName,
                Integer quantity,
                BigDecimal price,
                String imageDataUrl) {
}

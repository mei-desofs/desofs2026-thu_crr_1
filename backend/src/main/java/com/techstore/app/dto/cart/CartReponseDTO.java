package com.techstore.app.dto.cart;

import java.util.Map;
import java.util.UUID;

public record CartReponseDTO (
        Map<UUID, CartProductDTO> products
) {
}

package com.techstore.app.domain.cart;

import com.techstore.app.exception.BusinessException;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@EqualsAndHashCode
@Getter
public class CartItemId {

    private static final long serialVersionUID = 1L;

    private UUID id;

    protected CartItemId() {}

    public CartItemId(UUID id) {
        if (id == null) {
            throw new BusinessException("Cart Item ID cannot be null.");
        }
        this.id = id;
    }

    public static CartItemId newId() {
        return new CartItemId(UUID.randomUUID());
    }

    public static CartItemId fromString(String value) {
        return new CartItemId(UUID.fromString(value));
    }

}

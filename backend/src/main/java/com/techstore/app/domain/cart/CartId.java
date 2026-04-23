package com.techstore.app.domain.cart;

import com.techstore.app.exception.BusinessException;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@EqualsAndHashCode
@Getter
public class CartId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    protected CartId() {}

    public CartId(UUID id) {
        if (id == null) {
            throw new BusinessException("Cart ID cannot be null.");
        }
        this.id = id;
    }

    public static CartId newId() {
        return new CartId(UUID.randomUUID());
    }

    public static CartId fromString(String value) {
        return new CartId(UUID.fromString(value));
    }
}

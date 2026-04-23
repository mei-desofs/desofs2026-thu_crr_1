package com.techstore.app.domain.order;

import com.techstore.app.exception.BusinessException;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@EqualsAndHashCode
@Getter
public class OrderItemId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    protected OrderItemId() {}

    public OrderItemId(UUID id) {
        if (id == null) {
            throw new BusinessException("Order Item ID cannot be null.");
        }
        this.id = id;
    }

    public static OrderItemId newId() {
        return new OrderItemId(UUID.randomUUID());
    }

    public static OrderItemId fromString(String value) {
        return new OrderItemId(UUID.fromString(value));
    }
}

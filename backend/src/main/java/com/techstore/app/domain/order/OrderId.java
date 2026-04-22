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
public class OrderId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    protected OrderId() {}

    public OrderId(UUID id) {
        if (id == null) {
            throw new BusinessException("Order ID cannot be null.");
        }
        this.id = id;
    }

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId fromString(String value) {
        return new OrderId(UUID.fromString(value));
    }
}

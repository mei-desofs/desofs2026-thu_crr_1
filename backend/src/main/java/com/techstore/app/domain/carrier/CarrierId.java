package com.techstore.app.domain.carrier;

import com.techstore.app.exception.BusinessException;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;
@Getter
@EqualsAndHashCode
@Embeddable
public class CarrierId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    protected CarrierId() {
        // For JPA
    }

    public CarrierId(UUID id) {
        if (id == null) {
            throw new BusinessException("Customer ID cannot be null.");
        }
        this.id = id;
    }

    public static CarrierId newId() {
        return new CarrierId(UUID.randomUUID());
    }

    public static CarrierId fromString(String value) {
        return new CarrierId(UUID.fromString(value));
    }
}

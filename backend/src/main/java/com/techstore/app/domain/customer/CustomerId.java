package com.techstore.app.domain.customer;

import java.io.Serializable;
import java.util.UUID;

import com.techstore.app.exception.BusinessException;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Embeddable
public class CustomerId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    protected CustomerId() {
        // For JPA
    }

    public CustomerId(UUID id) {
        if (id == null) {
            throw new BusinessException("Customer ID cannot be null.");
        }
        this.id = id;
    }

    public static CustomerId newId() {
        return new CustomerId(UUID.randomUUID());
    }

    public static CustomerId fromString(String value) {
        return new CustomerId(UUID.fromString(value));
    }
}

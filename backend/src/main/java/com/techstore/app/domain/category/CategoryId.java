package com.techstore.app.domain.category;

import com.techstore.app.exception.BusinessException;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@EqualsAndHashCode
@Getter
public class CategoryId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    protected CategoryId() {}

    public CategoryId(UUID id) {
        if (id == null) {
            throw new BusinessException("Customer ID cannot be null.");
        }
        this.id = id;
    }

    public static CategoryId newId() {
        return new CategoryId(UUID.randomUUID());
    }

    public static CategoryId fromString(String value) {
        return new CategoryId(UUID.fromString(value));
    }
}
package com.techstore.app.domain.product;

import com.techstore.app.exception.BusinessException;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@EqualsAndHashCode
@Getter
public class ProductId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    protected ProductId() {}

    public ProductId(UUID id) {
        if (id == null) {
            throw new BusinessException("Product ID cannot be null.");
        }
        this.id = id;
    }

    public static ProductId newId() {
        return new ProductId(UUID.randomUUID());
    }

    public static ProductId fromString(String value) {
        return new ProductId(UUID.fromString(value));
    }
}

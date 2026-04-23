package com.techstore.app.domain.shared;

import com.techstore.app.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@EqualsAndHashCode
@Getter
public class Quantity {

    @DecimalMin("0")
    @Column(nullable = false)
    private Integer quantity;

    protected Quantity() {}

    public Quantity(Integer quantity) {
        if (!isValid(quantity)) {
            throw new BusinessException("Quantity must be zero or positive");
        }
        this.quantity = quantity;
    }

    private boolean isValid(Integer value) {
        return value != null && value > 0;
    }
}

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
  
    public void incrementQuantity(Integer quantity) {
        if (quantity <= 0) {
        throw new BusinessException("Quantity increment must be positive");
    }
    this.quantity += quantity;  
    }

    public void decrementQuantity(Integer quantity) {
        if (quantity <= 0) {
        throw new BusinessException("Quantity decrement must be positive");
    }
    if (this.quantity - quantity < 0) {
        throw new BusinessException("Cannot have negative quantity");
    }
    this.quantity -= quantity;
    }
}

package com.techstore.app.domain.shared;

import com.techstore.app.exception.BusinessException;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
public class Money {

    @DecimalMin("0.0")
    @Column(nullable = false)
    private BigDecimal value;

    protected Money() {}

    public Money(BigDecimal value) {
        if (!isValid(value)) {
            throw new BusinessException("Value is null or zero or negative");
        }

        this.value = value;
    }

    public void setValue(BigDecimal value) {
        if (!isValid(value)) {
            throw new BusinessException("Value is null or zero or negative");
        }

        this.value = value;
    }

    private boolean isValid(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }
}

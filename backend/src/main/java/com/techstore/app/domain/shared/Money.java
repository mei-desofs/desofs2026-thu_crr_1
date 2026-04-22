package com.techstore.app.domain.shared;

import com.techstore.app.exception.BusinessException;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@Embeddable
@EqualsAndHashCode
@Getter
public class Money {

    @DecimalMin("0.0")
    @Column(nullable = false)
    private BigDecimal moneyValue;

    protected Money() {}

    public Money(BigDecimal moneyValue) {
        if (!isValid(moneyValue)) {
            throw new BusinessException("Value is null or zero or negative");
        }

        this.moneyValue = moneyValue;
    }

    private boolean isValid(BigDecimal moneyValue) {
        return moneyValue != null && moneyValue.compareTo(BigDecimal.ZERO) > 0;
    }
}

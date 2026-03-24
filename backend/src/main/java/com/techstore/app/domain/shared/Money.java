package com.techstore.app.domain.shared;

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

    public Money() {
    }

    public Money(BigDecimal value) {
        this.value = value;
    }
}

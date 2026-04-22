package com.techstore.app.domain.product;

import com.techstore.app.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@EqualsAndHashCode
@Getter
public class ProductName {

    @NotBlank
    @Size(min = 2, max = 50)
    @Column(nullable = false, unique = true)
    private String productName;

    protected ProductName() {}

    public ProductName(String productName) {
        if (!isValid(productName)) {
            throw new BusinessException("Invalid category name");
        }
        this.productName = normalize(productName);
    }

    private boolean isValid(String productName) {
        return productName != null
                && !productName.trim().isEmpty()
                && productName.trim().length() >= 2
                && productName.trim().length() <= 50;
    }

    private String normalize(String productName) {
        return productName.trim();
    }
}

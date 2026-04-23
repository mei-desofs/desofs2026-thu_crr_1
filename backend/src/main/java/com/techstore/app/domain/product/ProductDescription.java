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
public class ProductDescription {

    @NotBlank
    @Size(min = 10, max = 1000)
    @Column(nullable = true, columnDefinition = "TEXT")
    private String description;

    protected ProductDescription() {}

    public ProductDescription(String description) {
        if (!isValid(description)) {
            throw new BusinessException("Invalid product description");
        }
        this.description = normalize(description);
    }

    private boolean isValid(String description) {
        return description != null
                && !description.trim().isEmpty()
                && description.trim().length() >= 10
                && description.trim().length() <= 1000;
    }

    private String normalize(String description) {
        return description.trim();
    }
}
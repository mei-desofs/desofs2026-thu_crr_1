package com.techstore.app.domain.category;

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
public class CategoryName {

    @NotBlank
    @Size(min = 2, max = 50)
    @Column(nullable = false, unique = true)
    private String categoryName;

    protected CategoryName() {}

    public CategoryName(String categoryName) {
        if (!isValid(categoryName)) {
            throw new BusinessException("Invalid category name");
        }
        this.categoryName = normalize(categoryName);
    }

    private boolean isValid(String categoryName) {
        return categoryName != null
                && !categoryName.trim().isEmpty()
                && categoryName.trim().length() >= 2
                && categoryName.trim().length() <= 50;
    }

    private String normalize(String categoryName) {
        return categoryName.trim();
    }
}
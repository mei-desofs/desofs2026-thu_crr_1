package com.techstore.app.domain.category;

import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryTest {

    @Test
    void shouldCreateCategoryWithName() {
        Category category = new Category("Laptops");

        assertEquals("Laptops", category.getName().getCategoryName());
    }

    @Test
    void shouldThrowExceptionWhenCreatingCategoryWithBlankName() {
        assertThrows(BusinessException.class, () -> new Category("   "));
    }

    @Test
    void shouldThrowExceptionWhenCreatingCategoryWithInvalidCharacters() {
        assertThrows(BusinessException.class, () -> new Category("###"));
    }

    @Test
    void shouldCreateCategoryWithDefaultConstructor() {
        Category category = new Category();

        assertNull(category.getId());
        assertNull(category.getName());
    }

    @Test
    void shouldReturnTrueWhenCategoryNameIsValid() {
        Category category = new Category();

        boolean isValid = category.isValid("Gaming Laptops 2026");

        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseWhenCategoryNameIsInvalid() {
        Category category = new Category();

        boolean isValid = category.isValid("###");

        assertFalse(isValid);
    }
}

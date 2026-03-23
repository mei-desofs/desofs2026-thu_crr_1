package com.techstore.app.domain.category;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CategoryTest {

    @Test
    void shouldCreateCategoryWithName() {
        Category category = new Category("Laptops");

        assertEquals("Laptops", category.getName());
    }

    @Test
    void shouldAllowUpdatingFields() {
        Category category = new Category();

        category.setId(10L);
        category.setName("Phones");

        assertEquals(10L, category.getId());
        assertEquals("Phones", category.getName());
    }

    @Test
    void shouldCreateCategoryWithDefaultConstructor() {
        Category category = new Category();

        assertNull(category.getId());
        assertNull(category.getName());
    }
}

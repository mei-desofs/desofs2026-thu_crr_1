package com.techstore.app.domain.product;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTest {

    @Test
    void shouldCreateProductWithAllFields() {
        Category category = new Category("Accessories");
        Money price = new Money(new BigDecimal("49.99"));

        Product product = new Product("Mouse", "Wireless mouse", price, category);

        assertEquals("Mouse", product.getName());
        assertEquals("Wireless mouse", product.getDescription());
        assertEquals(price, product.getPrice());
        assertEquals(category, product.getCategory());
    }

    @Test
    void shouldThrowExceptionWhenCreatingProductWithBlankName() {
        Category category = new Category("Accessories");
        Money price = new Money(new BigDecimal("49.99"));

        assertThrows(BusinessException.class,
                () -> new Product("   ", "Wireless mouse", price, category));
    }

    @Test
    void shouldThrowExceptionWhenCreatingProductWithInvalidCharactersInName() {
        Category category = new Category("Accessories");
        Money price = new Money(new BigDecimal("49.99"));

        assertThrows(BusinessException.class,
                () -> new Product("@@@", "Wireless mouse", price, category));
    }

    @Test
    void shouldAllowUpdatingFields() {
        Product product = new Product();
        Category category = new Category("Monitors");
        Money price = new Money(new BigDecimal("199.90"));

        product.setId(5L);
        product.setName("27 inch Monitor");
        product.setDescription("IPS panel");
        product.setPrice(price);
        product.setCategory(category);

        assertEquals(5L, product.getId());
        assertEquals("27 inch Monitor", product.getName());
        assertEquals("IPS panel", product.getDescription());
        assertEquals(price, product.getPrice());
        assertEquals(category, product.getCategory());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingProductNameWithBlankValue() {
        Product product = new Product();

        assertThrows(BusinessException.class, () -> product.setName("   "));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingProductNameWithInvalidCharacters() {
        Product product = new Product();

        assertThrows(BusinessException.class, () -> product.setName("@@@"));
    }

    @Test
    void shouldCreateProductWithDefaultConstructor() {
        Product product = new Product();

        assertNull(product.getId());
        assertNull(product.getName());
        assertNull(product.getDescription());
        assertNull(product.getPrice());
        assertNull(product.getCategory());
    }

    @Test
    void shouldReturnTrueWhenProductNameIsValid() {
        Product product = new Product();

        boolean isValid = product.isNameValid("Ultra Laptop 15");

        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseWhenProductNameIsInvalid() {
        Product product = new Product();

        boolean isValid = product.isNameValid("@@@");

        assertFalse(isValid);
    }
}

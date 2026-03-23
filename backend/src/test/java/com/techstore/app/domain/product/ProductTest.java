package com.techstore.app.domain.product;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    void shouldCreateProductWithDefaultConstructor() {
        Product product = new Product();

        assertNull(product.getId());
        assertNull(product.getName());
        assertNull(product.getDescription());
        assertNull(product.getPrice());
        assertNull(product.getCategory());
    }
}

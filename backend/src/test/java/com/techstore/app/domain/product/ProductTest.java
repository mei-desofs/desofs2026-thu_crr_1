package com.techstore.app.domain.product;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    @Test
    void shouldCreateProductWithAllFields() {
        Category category = new Category("Accessories");
        Money price = new Money(new BigDecimal("49.99"));

        Product product = new Product("Mouse", "Wireless mouse", price, category);

        assertEquals("Mouse", product.getName().getProductName());
        assertEquals("Wireless mouse", product.getDescription().getDescription());
        assertEquals(price.getMoneyValue(), product.getPrice().getMoneyValue());
        assertEquals(category.getName().getCategoryName(), product.getCategory().getName().getCategoryName());
    }

    @Test
    void shouldThrowExceptionWhenCreatingProductWithBlankName() {
        Category category = new Category("Accessories");
        Money price = new Money(new BigDecimal("49.99"));

        assertThrows(BusinessException.class,
                () -> new Product("   ", "Wireless mouse", price, category));
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

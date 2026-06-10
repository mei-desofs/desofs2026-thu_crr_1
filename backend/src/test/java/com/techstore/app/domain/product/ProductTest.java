package com.techstore.app.domain.product;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.domain.shared.Quantity;
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

        Product product = new Product("Mouse", "Wireless mouse", price, category, new Quantity(100));

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
                () -> new Product("   ", "Wireless mouse", price, category, new Quantity(100)));
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
    void shouldUpdateProductName() {
        Category category = new Category("Accessories");
        Product product = new Product("Mouse", "Wireless mouse device", new Money(new BigDecimal("49.99")), category,
                new Quantity(10));

        product.updateName("Gaming Mouse");

        assertEquals("Gaming Mouse", product.getName().getProductName());
    }

    @Test
    void shouldThrowWhenUpdatingNameToInvalid() {
        Category category = new Category("Accessories");
        Product product = new Product("Mouse", "Wireless mouse device", new Money(new BigDecimal("49.99")), category,
                new Quantity(10));

        assertThrows(BusinessException.class, () -> product.updateName("A"));
    }

    @Test
    void shouldUpdateProductDescription() {
        Category category = new Category("Accessories");
        Product product = new Product("Mouse", "Wireless mouse device", new Money(new BigDecimal("49.99")), category,
                new Quantity(10));

        product.updateDescription("High-precision wireless gaming mouse");

        assertEquals("High-precision wireless gaming mouse", product.getDescription().getDescription());
    }

    @Test
    void shouldThrowWhenUpdatingDescriptionToTooShort() {
        Category category = new Category("Accessories");
        Product product = new Product("Mouse", "Wireless mouse device", new Money(new BigDecimal("49.99")), category,
                new Quantity(10));

        assertThrows(BusinessException.class, () -> product.updateDescription("Too short"));
    }

    @Test
    void shouldUpdateProductPrice() {
        Category category = new Category("Accessories");
        Product product = new Product("Mouse", "Wireless mouse device", new Money(new BigDecimal("49.99")), category,
                new Quantity(10));

        product.updatePrice(new BigDecimal("79.99"));

        assertEquals(new BigDecimal("79.99"), product.getPrice().getMoneyValue());
    }

    @Test
    void shouldThrowWhenUpdatingPriceToNegative() {
        Category category = new Category("Accessories");
        Product product = new Product("Mouse", "Wireless mouse device", new Money(new BigDecimal("49.99")), category,
                new Quantity(10));

        assertThrows(BusinessException.class, () -> product.updatePrice(new BigDecimal("-1.00")));
    }

    @Test
    void shouldUpdateProductCategory() {
        Category oldCategory = new Category("Accessories");
        Category newCategory = new Category("Gaming");
        Product product = new Product("Mouse", "Wireless mouse device", new Money(new BigDecimal("49.99")), oldCategory,
                new Quantity(10));

        product.updateCategory(newCategory);

        assertEquals("Gaming", product.getCategory().getName().getCategoryName());
    }

    @Test
    void shouldUpdateProductStockQuantity() {
        Category category = new Category("Accessories");
        Product product = new Product("Mouse", "Wireless mouse device", new Money(new BigDecimal("49.99")), category,
                new Quantity(10));

        product.updateStockQuantity(250);

        assertEquals(250, product.getStockQuantity().getQuantity());
    }

    @Test
    void shouldThrowWhenUpdatingStockToNegative() {
        Category category = new Category("Accessories");
        Product product = new Product("Mouse", "Wireless mouse device", new Money(new BigDecimal("49.99")), category,
                new Quantity(10));

        assertThrows(BusinessException.class, () -> product.updateStockQuantity(-5));
    }
}

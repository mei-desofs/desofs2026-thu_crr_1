package com.techstore.app.domain.product;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.domain.shared.Quantity;
import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

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
    void shouldUpdateStockSuccessfully() {
        Category category = new Category("Accessories");
        Money price = new Money(new BigDecimal("49.99"));
        Product product = new Product("Mouse", "Wireless mouse", price, category, new Quantity(100));

        product.updateStock(new Quantity(75));

        assertEquals(75, product.getStockQuantity().getQuantity());
    }



    @Test
    void shouldUpdateStockToHigherQuantity() {
        Category category = new Category("Accessories");
        Money price = new Money(new BigDecimal("49.99"));
        Product product = new Product("Mouse", "Wireless mouse", price, category, new Quantity(50));

        product.updateStock(new Quantity(200));

        assertEquals(200, product.getStockQuantity().getQuantity());
    }

    @Test
    void shouldDecreaseStockSuccessfully() {
        Category category = new Category("Accessories");
        Money price = new Money(new BigDecimal("49.99"));
        Product product = new Product("Mouse", "Wireless mouse", price, category, new Quantity(100));

        product.decreaseStock(new Quantity(25));

        assertEquals(75, product.getStockQuantity().getQuantity());
    }

    @Test
    void shouldThrowWhenDecreasingStockMoreThanAvailable() {
        Category category = new Category("Accessories");
        Money price = new Money(new BigDecimal("49.99"));
        Product product = new Product("Mouse", "Wireless mouse", price, category, new Quantity(50));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> product.decreaseStock(new Quantity(100)));

        assertTrue(exception.getMessage().contains("Not enough stock"));
    }
}
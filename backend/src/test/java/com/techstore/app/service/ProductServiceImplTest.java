package com.techstore.app.service;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.dto.ProductCreationResponse;
import com.techstore.app.dto.ProductRequestDTO;
import com.techstore.app.repository.CategoryRepository;
import com.techstore.app.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void shouldSaveProductAndReturnResponse() {
        ProductRequestDTO dto = mockProductRequest("Keyboard", "Mechanical keyboard", new BigDecimal("89.99"), 2L);
        Category category = new Category("Peripherals");
        category.setId(2L);

        Product savedProduct = new Product("Keyboard", "Mechanical keyboard", new Money(new BigDecimal("89.99")), category);
        savedProduct.setId(100L);

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductCreationResponse response = productService.save(dto);

        assertEquals(100L, response.id());
        assertEquals("Keyboard", response.name());
        assertEquals("Mechanical keyboard", response.description());
        assertEquals(new BigDecimal("89.99"), response.price());
        assertEquals("Peripherals", response.categoryName());
    }

    @Test
    void shouldThrowWhenCategoryDoesNotExist() {
        ProductRequestDTO dto = mockProductRequestWithCategoryId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.save(dto));

        assertEquals("Category not found", exception.getMessage());
    }

    private ProductRequestDTO mockProductRequest(String name, String description, BigDecimal price, Long categoryId) {
        ProductRequestDTO dto = mock(ProductRequestDTO.class);
        when(dto.name()).thenReturn(name);
        when(dto.description()).thenReturn(description);
        when(dto.price()).thenReturn(price);
        when(dto.categoryId()).thenReturn(categoryId);
        return dto;
    }

    private ProductRequestDTO mockProductRequestWithCategoryId(Long categoryId) {
        ProductRequestDTO dto = mock(ProductRequestDTO.class);
        when(dto.categoryId()).thenReturn(categoryId);
        return dto;
    }
}

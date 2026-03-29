package com.techstore.app.service;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.dto.ProductResponseDTO;
import com.techstore.app.dto.ProductRequestDTO;
import com.techstore.app.repository.CategoryRepository;
import com.techstore.app.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
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

        ProductResponseDTO response = productService.save(dto);

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

    @Test
    void shouldFindProductsByNameAndMapToResponse() {
        Category peripherals = new Category("Peripherals");
        Category audio = new Category("Audio");

        Product keyboard = new Product("Keyboard", "Mechanical keyboard", new Money(new BigDecimal("89.99")), peripherals);
        keyboard.setId(100L);
        Product headset = new Product("Keyboard", "Gaming headset", new Money(new BigDecimal("59.90")), audio);
        headset.setId(101L);

        when(productRepository.findByName("Keyboard")).thenReturn(List.of(keyboard, headset));

        List<ProductResponseDTO> response = productService.findByName("Keyboard");

        assertEquals(2, response.size());
        assertEquals(100L, response.get(0).id());
        assertEquals("Keyboard", response.get(0).name());
        assertEquals("Mechanical keyboard", response.get(0).description());
        assertEquals(new BigDecimal("89.99"), response.get(0).price());
        assertEquals("Peripherals", response.get(0).categoryName());

        assertEquals(101L, response.get(1).id());
        assertEquals("Keyboard", response.get(1).name());
        assertEquals("Gaming headset", response.get(1).description());
        assertEquals(new BigDecimal("59.90"), response.get(1).price());
        assertEquals("Audio", response.get(1).categoryName());
    }

    @Test
    void shouldFindProductsByNameLikeAndMapToResponsePage() {
        Category peripherals = new Category("Peripherals");
        Category audio = new Category("Audio");

        Product keyboard = new Product("Keyboard", "Mechanical keyboard", new Money(new BigDecimal("89.99")), peripherals);
        keyboard.setId(100L);
        Product headset = new Product("Gaming Keyboard", "RGB keyboard", new Money(new BigDecimal("59.90")), audio);
        headset.setId(101L);

        Pageable pageable = PageRequest.of(0, 5);
        Page<Product> productsPage = new PageImpl<>(List.of(keyboard, headset), pageable, 2);

        when(productRepository.findByNameLike("Key", pageable)).thenReturn(productsPage);

        Page<ProductResponseDTO> response = productService.findByNameLike("Key", pageable);

        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(2, response.getContent().size());

        assertEquals(100L, response.getContent().get(0).id());
        assertEquals("Keyboard", response.getContent().get(0).name());
        assertEquals("Mechanical keyboard", response.getContent().get(0).description());
        assertEquals(new BigDecimal("89.99"), response.getContent().get(0).price());
        assertEquals("Peripherals", response.getContent().get(0).categoryName());

        assertEquals(101L, response.getContent().get(1).id());
        assertEquals("Gaming Keyboard", response.getContent().get(1).name());
        assertEquals("RGB keyboard", response.getContent().get(1).description());
        assertEquals(new BigDecimal("59.90"), response.getContent().get(1).price());
        assertEquals("Audio", response.getContent().get(1).categoryName());
    }

    @Test
    void shouldReturnEmptyPageWhenFindByNameLikeHasNoMatches() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productRepository.findByNameLike("unknown", pageable)).thenReturn(emptyPage);

        Page<ProductResponseDTO> response = productService.findByNameLike("unknown", pageable);

        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getContent().size());
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

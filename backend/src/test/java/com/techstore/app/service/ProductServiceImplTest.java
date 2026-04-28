package com.techstore.app.service;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.category.CategoryId;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.product.ProductName;
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
import java.util.UUID;

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
        UUID categoryId = UUID.randomUUID();
        ProductRequestDTO dto = mockProductRequest("Keyboard", "Mechanical keyboard", new BigDecimal("89.99"), categoryId);
        Category category = new Category("Peripherals");

        Product savedProduct = new Product("Keyboard", "Mechanical keyboard", new Money(new BigDecimal("89.99")), category);

        when(categoryRepository.findById(new CategoryId(categoryId))).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponseDTO response = productService.save(dto);

        assertEquals("Keyboard", response.name());
        assertEquals("Mechanical keyboard", response.description());
        assertEquals(new BigDecimal("89.99"), response.price());
        assertEquals("Peripherals", response.categoryName());
    }

    @Test
    void shouldThrowWhenCategoryDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        ProductRequestDTO dto = mockProductRequestWithCategoryId(uuid);

        when(categoryRepository.findById(new CategoryId(uuid))).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.save(dto));

        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void shouldFindProductsByNameAndMapToResponse() {
        Category peripherals = new Category("Peripherals");
        Category audio = new Category("Audio");

        Product keyboard = new Product("Keyboard", "Mechanical keyboard", new Money(new BigDecimal("89.99")), peripherals);
        Product headset = new Product("Keyboard", "Gaming headset", new Money(new BigDecimal("59.90")), audio);

        when(productRepository.findByName(new ProductName("Keyboard"))).thenReturn(List.of(keyboard, headset));

        List<ProductResponseDTO> response = productService.findByName(new ProductName("Keyboard"));

        assertEquals(2, response.size());
        assertEquals("Keyboard", response.get(0).name());
        assertEquals("Mechanical keyboard", response.get(0).description());
        assertEquals(new BigDecimal("89.99"), response.get(0).price());
        assertEquals("Peripherals", response.get(0).categoryName());

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
        Product headset = new Product("Gaming Keyboard", "RGB keyboard", new Money(new BigDecimal("59.90")), audio);

        Pageable pageable = PageRequest.of(0, 5);
        Page<Product> productsPage = new PageImpl<>(List.of(keyboard, headset), pageable, 2);

        when(productRepository.findByNameLike("Key", pageable)).thenReturn(productsPage);

        Page<ProductResponseDTO> response = productService.findByNameLike(new ProductName("Key"), pageable);

        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(2, response.getContent().size());

        assertEquals("Keyboard", response.getContent().get(0).name());
        assertEquals("Mechanical keyboard", response.getContent().get(0).description());
        assertEquals(new BigDecimal("89.99"), response.getContent().get(0).price());
        assertEquals("Peripherals", response.getContent().get(0).categoryName());

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

        Page<ProductResponseDTO> response = productService.findByNameLike(new ProductName("unknown"), pageable);

        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getContent().size());
    }

    @Test
    void shouldFindAllAndMapToResponsePage() {
        Category peripherals = new Category("Peripherals");
        Category audio = new Category("Audio");

        Product keyboard = new Product("Keyboard", "Mechanical keyboard", new Money(new BigDecimal("89.99")), peripherals);
        Product headset = new Product("Headset", "Gaming headset", new Money(new BigDecimal("59.90")), audio);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productsPage = new PageImpl<>(List.of(keyboard, headset), pageable, 2);

        when(productRepository.findAll(pageable)).thenReturn(productsPage);

        Page<ProductResponseDTO> response = productService.findAll(pageable);

        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(2, response.getContent().size());

        assertEquals("Keyboard", response.getContent().get(0).name());
        assertEquals("Mechanical keyboard", response.getContent().get(0).description());
        assertEquals(new BigDecimal("89.99"), response.getContent().get(0).price());
        assertEquals("Peripherals", response.getContent().get(0).categoryName());

        assertEquals("Headset", response.getContent().get(1).name());
        assertEquals("Gaming headset", response.getContent().get(1).description());
        assertEquals(new BigDecimal("59.90"), response.getContent().get(1).price());
        assertEquals("Audio", response.getContent().get(1).categoryName());
    }

    @Test
    void shouldReturnEmptyPageWhenFindAllHasNoMatches() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<ProductResponseDTO> response = productService.findAll(pageable);

        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getContent().size());
    }

    private ProductRequestDTO mockProductRequest(String name, String description, BigDecimal price, UUID categoryId) {
        ProductRequestDTO dto = mock(ProductRequestDTO.class);
        when(dto.name()).thenReturn(name);
        when(dto.description()).thenReturn(description);
        when(dto.price()).thenReturn(price);
        when(dto.categoryId()).thenReturn(categoryId);
        return dto;
    }

    private ProductRequestDTO mockProductRequestWithCategoryId(UUID categoryId) {
        ProductRequestDTO dto = mock(ProductRequestDTO.class);
        when(dto.categoryId()).thenReturn(categoryId);
        return dto;
    }
}

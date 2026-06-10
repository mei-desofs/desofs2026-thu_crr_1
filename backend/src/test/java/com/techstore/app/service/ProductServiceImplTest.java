package com.techstore.app.service;

import com.techstore.app.config.FileUploadConfig;
import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.category.CategoryId;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.product.ProductName;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.domain.shared.Quantity;
import com.techstore.app.dto.ProductResponseDTO;
import com.techstore.app.dto.ProductRequestDTO;
import com.techstore.app.logger.ProductAuditLogger;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

        @Mock
        private ProductRepository productRepository;

        @Mock
        private CategoryRepository categoryRepository;

        @Mock
        private ProductAuditLogger productAuditLogger;

        @Mock
        private FileUploadConfig fileUploadConfig;

        @InjectMocks
        private ProductServiceImpl productService;

        @Test
        void shouldSaveProductAndReturnResponse() throws IOException {
                UUID categoryId = UUID.randomUUID();
                ProductRequestDTO dto = mockProductRequest("Keyboard", "Mechanical keyboard", new BigDecimal("89.99"),
                                100,
                                categoryId);
                Category category = new Category("Peripherals");

                Product savedProduct = new Product("Keyboard", "Mechanical keyboard",
                                new Money(new BigDecimal("89.99")),
                                category, new Quantity(100));

                when(categoryRepository.findById(new CategoryId(categoryId))).thenReturn(Optional.of(category));
                when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

                ProductResponseDTO response = productService.save(dto, null);

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

                RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.save(dto, null));

                assertEquals("Category not found", exception.getMessage());
        }

        @Test
        void shouldFindProductsByNameAndMapToResponse() {
                Category peripherals = new Category("Peripherals");
                Category audio = new Category("Audio");

                Product keyboard = new Product("Keyboard", "Mechanical keyboard", new Money(new BigDecimal("89.99")),
                                peripherals, new Quantity(100));
                Product headset = new Product("Keyboard", "Gaming headset", new Money(new BigDecimal("59.90")), audio,
                                new Quantity(100));

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

                Product keyboard = new Product("Keyboard", "Mechanical keyboard", new Money(new BigDecimal("89.99")),
                                peripherals, new Quantity(100));
                Product headset = new Product("Gaming Keyboard", "RGB keyboard", new Money(new BigDecimal("59.90")),
                                audio,
                                new Quantity(100));

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

                Product keyboard = new Product("Keyboard", "Mechanical keyboard", new Money(new BigDecimal("89.99")),
                                peripherals, new Quantity(100));
                Product headset = new Product("Headset", "Gaming headset", new Money(new BigDecimal("59.90")), audio,
                                new Quantity(100));

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

        private ProductRequestDTO mockProductRequest(String name, String description, BigDecimal price,
                        Integer stockQuantity, UUID categoryId) {
                ProductRequestDTO dto = mock(ProductRequestDTO.class);
                when(dto.name()).thenReturn(name);
                when(dto.description()).thenReturn(description);
                when(dto.price()).thenReturn(price);
                when(dto.categoryId()).thenReturn(categoryId);
                when(dto.stockQuantity()).thenReturn(stockQuantity);
                return dto;
        }

        private ProductRequestDTO mockProductRequestWithCategoryId(UUID categoryId) {
                ProductRequestDTO dto = mock(ProductRequestDTO.class);
                when(dto.categoryId()).thenReturn(categoryId);
                return dto;
        }

        @Test
        void shouldUpdateProductNameAndReturnResponse() {
                UUID productId = UUID.randomUUID();
                Category category = new Category("Peripherals");
                Product existing = new Product("OldName", "Some description here", new Money(new BigDecimal("50.00")),
                                category,
                                new Quantity(10));

                ProductUpdateDTO dto = mock(ProductUpdateDTO.class);
                when(dto.name()).thenReturn("NewName");
                when(dto.description()).thenReturn(null);
                when(dto.price()).thenReturn(null);
                when(dto.stockQuantity()).thenReturn(null);
                when(dto.categoryId()).thenReturn(null);

                when(productRepository.findById_Id(productId)).thenReturn(existing);
                when(productRepository.save(any(Product.class))).thenReturn(existing);

                ProductResponseDTO response = productService.update(productId, dto, "manager-001");

                assertNotNull(response);
                assertEquals("NewName", response.name());
        }

        @Test
        void shouldUpdateProductPriceAndReturnResponse() {
                UUID productId = UUID.randomUUID();
                Category category = new Category("Audio");
                Product existing = new Product("Headset", "Gaming headset model", new Money(new BigDecimal("59.90")),
                                category,
                                new Quantity(20));

                ProductUpdateDTO dto = mock(ProductUpdateDTO.class);
                when(dto.name()).thenReturn(null);
                when(dto.description()).thenReturn(null);
                when(dto.price()).thenReturn(new BigDecimal("79.90"));
                when(dto.stockQuantity()).thenReturn(null);
                when(dto.categoryId()).thenReturn(null);

                when(productRepository.findById_Id(productId)).thenReturn(existing);
                when(productRepository.save(any(Product.class))).thenReturn(existing);

                ProductResponseDTO response = productService.update(productId, dto, "manager-001");

                assertNotNull(response);
                assertEquals(new BigDecimal("79.90"), response.price());
        }

        @Test
        void shouldUpdateProductCategoryAndReturnResponse() {
                UUID productId = UUID.randomUUID();
                UUID newCategoryId = UUID.randomUUID();
                Category oldCategory = new Category("Peripherals");
                Category newCategory = new Category("Gaming");
                Product existing = new Product("Keyboard", "Mechanical keyboard model",
                                new Money(new BigDecimal("89.99")),
                                oldCategory, new Quantity(50));

                ProductUpdateDTO dto = mock(ProductUpdateDTO.class);
                when(dto.name()).thenReturn(null);
                when(dto.description()).thenReturn(null);
                when(dto.price()).thenReturn(null);
                when(dto.stockQuantity()).thenReturn(null);
                when(dto.categoryId()).thenReturn(newCategoryId);

                when(productRepository.findById_Id(productId)).thenReturn(existing);
                when(categoryRepository.findById(new CategoryId(newCategoryId))).thenReturn(Optional.of(newCategory));
                when(productRepository.save(any(Product.class))).thenReturn(existing);

                ProductResponseDTO response = productService.update(productId, dto, "manager-001");

                assertNotNull(response);
                assertEquals("Gaming", response.categoryName());
        }

        @Test
        void shouldThrowWhenUpdatingNonExistentProduct() {
                UUID productId = UUID.randomUUID();
                ProductUpdateDTO dto = mock(ProductUpdateDTO.class);

                when(productRepository.findById_Id(productId)).thenReturn(null);

                RuntimeException exception = assertThrows(RuntimeException.class,
                                () -> productService.update(productId, dto, "manager-001"));

                assertEquals("Product not found", exception.getMessage());
        }

        @Test
        void shouldThrowWhenUpdatingWithNonExistentCategory() {
                UUID productId = UUID.randomUUID();
                UUID badCategoryId = UUID.randomUUID();
                Category category = new Category("Peripherals");
                Product existing = new Product("Keyboard", "Mechanical keyboard model",
                                new Money(new BigDecimal("89.99")),
                                category, new Quantity(50));

                ProductUpdateDTO dto = mock(ProductUpdateDTO.class);
                when(dto.name()).thenReturn(null);
                when(dto.description()).thenReturn(null);
                when(dto.price()).thenReturn(null);
                when(dto.stockQuantity()).thenReturn(null);
                when(dto.categoryId()).thenReturn(badCategoryId);

                when(productRepository.findById_Id(productId)).thenReturn(existing);
                when(categoryRepository.findById(new CategoryId(badCategoryId))).thenReturn(Optional.empty());

                RuntimeException exception = assertThrows(RuntimeException.class,
                                () -> productService.update(productId, dto, "manager-001"));

                assertEquals("Category not found", exception.getMessage());
        }

        @Test
        void shouldCallAuditLoggerOnSuccessfulUpdate() {
                UUID productId = UUID.randomUUID();
                Category category = new Category("Peripherals");
                Product existing = new Product("Keyboard", "Mechanical keyboard model",
                                new Money(new BigDecimal("89.99")),
                                category, new Quantity(50));

                ProductUpdateDTO dto = mock(ProductUpdateDTO.class);
                when(dto.name()).thenReturn("Updated Keyboard");
                when(dto.description()).thenReturn(null);
                when(dto.price()).thenReturn(null);
                when(dto.stockQuantity()).thenReturn(null);
                when(dto.categoryId()).thenReturn(null);

                when(productRepository.findById_Id(productId)).thenReturn(existing);
                when(productRepository.save(any(Product.class))).thenReturn(existing);

                productService.update(productId, dto, "manager-001");

                verify(productAuditLogger).logProductUpdate(productId.toString(), "manager-001");
        }

        @Test
        void shouldCallAuditLoggerWithRealUserIdOnSave() {
                UUID categoryId = UUID.randomUUID();
                ProductRequestDTO dto = mockProductRequest("Monitor", "4K gaming monitor", new BigDecimal("399.99"), 5,
                                categoryId);
                Category category = new Category("Displays");
                Product saved = new Product("Monitor", "4K gaming monitor", new Money(new BigDecimal("399.99")),
                                category,
                                new Quantity(5));

                when(categoryRepository.findById(new CategoryId(categoryId))).thenReturn(Optional.of(category));
                when(productRepository.save(any(Product.class))).thenReturn(saved);

                productService.save(dto, "manager-007");

                verify(productAuditLogger).logProductCreation("Monitor", categoryId.toString(), "399.99",
                                "manager-007");
        }
}

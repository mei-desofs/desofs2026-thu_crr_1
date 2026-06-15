package com.techstore.app.controller;

import com.techstore.app.config.jwt.JWTAuthFilter;
import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.domain.shared.Quantity;
import com.techstore.app.repository.CategoryRepository;
import com.techstore.app.repository.ProductRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductUpdateIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    private String managerId;
    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() throws Exception {
        managerId = "manager-" + UUID.randomUUID();
        setUpJwtFilter();

        // Create unique categories and products for each test with only letters
        String randomId = System.nanoTime() + "" + (int) (Math.random() * 1000);
        String randomCategoryName = "TestCat" + randomId.substring(Math.max(0, randomId.length() - 10));
        testCategory = new Category(randomCategoryName);
        testCategory = categoryRepository.save(testCategory);

        String randomProductName = "TestProd" + randomId.substring(Math.max(0, randomId.length() - 10));
        testProduct = new Product(
                randomProductName,
                "Test product description with more detail",
                new Money(new BigDecimal("10.00")),
                testCategory,
                new Quantity(100));
        testProduct = productRepository.save(testProduct);
    }

    private void setUpJwtFilter() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    managerId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));

            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    private void setUpCustomerAuth() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "customer-user",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    void updateProductWithAllFields_Success() throws Exception {
        String newName = "Updated Product Name";
        String newDescription = "This is an updated product description";
        BigDecimal newPrice = new BigDecimal("25.99");
        Integer newStock = 50;

        String content = String.format(
                "name=%s&description=%s&price=%s&stockQuantity=%d&categoryId=%s",
                newName, newDescription, newPrice, newStock, testCategory.getId().getId());

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newName))
                .andExpect(jsonPath("$.description").value(newDescription))
                .andExpect(jsonPath("$.price").value(newPrice.doubleValue()))
                .andExpect(jsonPath("$.stockQuantity").value(newStock));

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updated.getName().getProductName()).isEqualTo(newName);
        assertThat(updated.getDescription().getDescription()).isEqualTo(newDescription);
        assertThat(updated.getPrice().getMoneyValue()).isEqualTo(newPrice);
        assertThat(updated.getStockQuantity().getQuantity()).isEqualTo(newStock);
    }

    @Test
    void updateProductNameOnly_Success() throws Exception {
        String newName = "New Product Name";
        String content = "name=" + newName;

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newName));

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updated.getName().getProductName()).isEqualTo(newName);
    }

    @Test
    void updateProductDescriptionOnly_Success() throws Exception {
        String newDescription = "This is a completely new and updated description";
        String content = "description=" + newDescription;

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(newDescription));

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updated.getDescription().getDescription()).isEqualTo(newDescription);
    }

    @Test
    void updateProductPriceOnly_Success() throws Exception {
        BigDecimal newPrice = new BigDecimal("99.99");
        String content = "price=" + newPrice;

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(newPrice.doubleValue()));

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updated.getPrice().getMoneyValue()).isEqualTo(newPrice);
    }

    @Test
    void updateProductStockOnly_Success() throws Exception {
        Integer newStock = 200;
        String content = "stockQuantity=" + newStock;

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(newStock));

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updated.getStockQuantity().getQuantity()).isEqualTo(newStock);
    }

    @Test
    void updateProductCategoryOnly_Success() throws Exception {
        Category newCategory = new Category("New Category");
        Category savedCategory = categoryRepository.save(newCategory);

        String content = "categoryId=" + savedCategory.getId().getId();

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("New Category"));

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updated.getCategory().getName().getCategoryName()).isEqualTo("New Category");
    }

    @Test
    void updateProductWithInvalidImage_Failure() throws Exception {
        // Create a non-image file
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.txt",
                "text/plain",
                "This is not an image".getBytes());

        mvc.perform(multipart("/products/{id}", testProduct.getId().getId())
                .file(file)
                .param("name", "Updated Name")
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateProductWithInvalidCategoryId_NotFound() throws Exception {
        UUID invalidCategoryId = UUID.randomUUID();
        String content = "categoryId=" + invalidCategoryId;

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    void updateProductWithNegativeStock_Failure() throws Exception {
        String content = "stockQuantity=-10";

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProductWithZeroStock_ValidationError() throws Exception {
        String content = "stockQuantity=0";

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProductNameTooShort_ValidationError() throws Exception {
        String content = "name=A";

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProductNameTooLong_ValidationError() throws Exception {
        String longName = "A".repeat(101);
        String content = "name=" + longName;

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProductDescriptionTooShort_ValidationError() throws Exception {
        String content = "description=Short";

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProductDescriptionTooLong_ValidationError() throws Exception {
        String longDescription = "A".repeat(1001);
        String content = "description=" + longDescription;

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProductNegativePrice_ValidationError() throws Exception {
        String content = "price=-10.00";

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProductPriceTooHigh_ValidationError() throws Exception {
        String content = "price=1000000.00";

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProductNotFound_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        String content = "name=Updated Name";

        mvc.perform(patch("/products/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void updateProductAsCustomer_Forbidden() throws Exception {
        setUpCustomerAuth();

        String content = "name=Updated Name";

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProductStockToMaximum_Success() throws Exception {
        String content = "stockQuantity=999999";

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(999999));
    }

    @Test
    void updateProductStockExceedsMaximum_ValidationError() throws Exception {
        String content = "stockQuantity=1000000";

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProductPriceToMaximum_Success() throws Exception {
        String content = "price=999999.99";

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(999999.99));
    }

    @Test
    void updateProductMultipleFields_Success() throws Exception {
        String newName = "Updated Product";
        BigDecimal newPrice = new BigDecimal("45.50");
        Integer newStock = 150;

        String content = String.format(
                "name=%s&price=%s&stockQuantity=%d",
                newName, newPrice, newStock);

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newName))
                .andExpect(jsonPath("$.price").value(newPrice.doubleValue()))
                .andExpect(jsonPath("$.stockQuantity").value(newStock));

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updated.getName().getProductName()).isEqualTo(newName);
        assertThat(updated.getPrice().getMoneyValue()).isEqualTo(newPrice);
        assertThat(updated.getStockQuantity().getQuantity()).isEqualTo(newStock);
    }

    @Test
    void updateProductWithZeroPrice_ValidationError() throws Exception {
        String content = "price=0.00";

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(content))
                .andExpect(status().isBadRequest());

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updated.getPrice().getMoneyValue()).isNotEqualTo(BigDecimal.ZERO);
    }

    @Test
    void updateProductEmptyUpdate_Success() throws Exception {
        Product original = testProduct;
        String originalName = original.getName().getProductName();

        mvc.perform(patch("/products/{id}", testProduct.getId().getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(""))
                .andExpect(status().isOk());

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updated.getName().getProductName()).isEqualTo(originalName);
    }
}

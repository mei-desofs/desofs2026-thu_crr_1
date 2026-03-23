package com.techstore.app.service;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.product.Product;
import com.techstore.app.dto.ProductResponseDTO;
import com.techstore.app.dto.ProductRequestDTO;
import com.techstore.app.mapper.ProductMapper;
import com.techstore.app.repository.CategoryRepository;
import com.techstore.app.repository.ProductRepository;
import com.techstore.app.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductResponseDTO save(ProductRequestDTO dto) {
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = ProductMapper.toEntity(dto, category);
        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Override
    public List<ProductResponseDTO> findByName(String productName) {
        List<Product> products = productRepository.findByName(productName);

        return products.stream().map(ProductMapper::toResponse)
                .toList();
    }
}

package com.techstore.app.service;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.category.CategoryId;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.product.ProductName;
import com.techstore.app.dto.ProductResponseDTO;
import com.techstore.app.dto.ProductRequestDTO;
import com.techstore.app.dto.ProductUpdateDTO;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.logger.ProductAuditLogger;
import com.techstore.app.mapper.ProductMapper;
import com.techstore.app.repository.CategoryRepository;
import com.techstore.app.repository.ProductRepository;
import com.techstore.app.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductAuditLogger productAuditLogger;

    @Override
    public ProductResponseDTO save(ProductRequestDTO dto, String userId) {
        try {
            Category category = categoryRepository.findById(new CategoryId(dto.categoryId()))
                    .orElseThrow(() -> new BusinessException("Category not found"));

            Product product = ProductMapper.toEntity(dto, category, dto.stockQuantity());
            ProductResponseDTO response = ProductMapper.toResponse(productRepository.save(product));

            productAuditLogger.logProductCreation(dto.name(), dto.categoryId().toString(), dto.price().toString(),
                    userId);

            return response;
        } catch (BusinessException e) {
            productAuditLogger.logProductCreationFailure(dto.name(), e.getMessage(), userId);
            throw e;
        }
    }

    @Override
    public List<ProductResponseDTO> findByName(ProductName productName) {
        List<Product> products = productRepository.findByName(productName);

        return products.stream().map(ProductMapper::toResponse)
                .toList();
    }

    @Override
    public Page<ProductResponseDTO> findByNameLike(ProductName productName, Pageable pageable) {
        Page<Product> products = productRepository.findByNameLike(productName.getProductName(), pageable);

        return products.map(ProductMapper::toResponse);
    }

    @Override
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);

        return products.map(ProductMapper::toResponse);
    }

    @Override
    public ProductResponseDTO update(UUID id, ProductUpdateDTO dto, String userId) {
        try {
            Product product = productRepository.findById_Id(id);
            if (product == null) {
                throw new BusinessException("Product not found");
            }

            if (dto.name() != null)
                product.updateName(dto.name());
            if (dto.description() != null)
                product.updateDescription(dto.description());
            if (dto.price() != null)
                product.updatePrice(dto.price());
            if (dto.stockQuantity() != null)
                product.updateStockQuantity(dto.stockQuantity());
            if (dto.categoryId() != null) {
                Category category = categoryRepository.findById(new CategoryId(dto.categoryId()))
                        .orElseThrow(() -> new BusinessException("Category not found"));
                product.updateCategory(category);
            }

            ProductResponseDTO response = ProductMapper.toResponse(productRepository.save(product));

            productAuditLogger.logProductUpdate(id.toString(), userId);

            return response;
        } catch (BusinessException e) {
            productAuditLogger.logProductUpdateFailure(id.toString(), e.getMessage(), userId);
            throw e;
        }
    }
}

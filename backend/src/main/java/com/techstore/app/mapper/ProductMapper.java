package com.techstore.app.mapper;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.dto.ProductResponseDTO;
import com.techstore.app.dto.ProductRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public static Product toEntity(ProductRequestDTO dto, Category category) {
        return new Product(
            dto.name(),
            dto.description(),
            new Money(dto.price()),
            category
        );
    }

    public static ProductResponseDTO toResponse(Product product) {
        return new ProductResponseDTO(
                product.getId().getId(),
                product.getName().getProductName(),
                product.getDescription().getDescription(),
                product.getPrice().getMoneyValue(),
                product.getCategory().getName().getCategoryName()
        );
    }
}
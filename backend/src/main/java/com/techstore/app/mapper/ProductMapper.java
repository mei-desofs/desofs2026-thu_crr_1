package com.techstore.app.mapper;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.dto.ProductCreationResponse;
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

    public static ProductCreationResponse toResponse(Product product) {
        return new ProductCreationResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice().getValue(),
                product.getCategory().getName()
        );
    }
}
package com.techstore.app.mapper;

import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.shared.Money;
import com.techstore.app.dto.ProductRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public static Product toEntity(ProductRequestDTO dto, Category category) {
        return new Product(
            dto.getName(),
            dto.getDescription(),
            new Money(dto.getPrice()),
            category
        );
    }
}
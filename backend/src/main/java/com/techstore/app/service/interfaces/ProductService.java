package com.techstore.app.service.interfaces;

import com.techstore.app.domain.product.Product;
import com.techstore.app.dto.ProductCreationResponse;
import com.techstore.app.dto.ProductRequestDTO;

public interface ProductService {

    ProductCreationResponse save(ProductRequestDTO productRequestDTO);

}

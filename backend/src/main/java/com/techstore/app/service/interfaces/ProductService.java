package com.techstore.app.service.interfaces;

import com.techstore.app.domain.product.Product;
import com.techstore.app.dto.ProductRequestDTO;

public interface ProductService {

    Product save(ProductRequestDTO productRequestDTO);

}

package com.techstore.app.service.interfaces;

import com.techstore.app.dto.ProductResponseDTO;
import com.techstore.app.dto.ProductRequestDTO;

import java.util.List;

public interface ProductService {

    ProductResponseDTO save(ProductRequestDTO productRequestDTO);
    List<ProductResponseDTO> findByName(String productName);

}

package com.techstore.app.service.interfaces;

import com.techstore.app.dto.ProductResponseDTO;
import com.techstore.app.dto.ProductRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    ProductResponseDTO save(ProductRequestDTO productRequestDTO);
    List<ProductResponseDTO> findByName(String productName);
    Page<ProductResponseDTO> findByNameLike(String productName, Pageable pageable);

}

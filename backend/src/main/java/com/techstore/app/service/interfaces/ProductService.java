package com.techstore.app.service.interfaces;

import com.techstore.app.domain.product.ProductName;
import com.techstore.app.dto.ProductResponseDTO;
import com.techstore.app.dto.ProductRequestDTO;
import com.techstore.app.dto.ProductUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponseDTO save(ProductRequestDTO productRequestDTO, org.springframework.web.multipart.MultipartFile image)
            throws IOException;

    List<ProductResponseDTO> findByName(ProductName productName);

    Page<ProductResponseDTO> findByNameLike(ProductName productName, Pageable pageable);

    Page<ProductResponseDTO> findAll(Pageable pageable);
}

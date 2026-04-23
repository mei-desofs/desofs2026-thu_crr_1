package com.techstore.app.controller;

import com.techstore.app.domain.product.ProductName;
import com.techstore.app.dto.ProductResponseDTO;
import com.techstore.app.dto.ProductRequestDTO;
import com.techstore.app.service.interfaces.ProductService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ProductResponseDTO save(@Valid @RequestBody ProductRequestDTO productRequestDTO) {
        return productService.save(productRequestDTO);
    }

    @GetMapping("/search")
    public Page<ProductResponseDTO> search(@RequestParam String productName,
                                           @ParameterObject @PageableDefault(size = 5, sort = "name") Pageable pageable) {
        return productService.findByNameLike(new ProductName(productName), pageable);
    }
}

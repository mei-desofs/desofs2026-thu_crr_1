package com.techstore.app.controller;

import com.techstore.app.dto.ProductCreationResponse;
import com.techstore.app.dto.ProductRequestDTO;
import com.techstore.app.service.interfaces.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ProductCreationResponse save(@Valid @RequestBody ProductRequestDTO productRequestDTO) {
        return productService.save(productRequestDTO);
    }
}

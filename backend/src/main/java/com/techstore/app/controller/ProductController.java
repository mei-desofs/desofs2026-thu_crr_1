package com.techstore.app.controller;

import com.techstore.app.config.ratelimit.annotation.RateLimit;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @RateLimit("create-product")
    @PostMapping
    public ProductResponseDTO save(@Valid @ModelAttribute ProductRequestDTO productRequestDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        return productService.save(productRequestDTO, image);
    }

    @RateLimit("search-products")
    @GetMapping("/search")
    public Page<ProductResponseDTO> search(@RequestParam String productName,
            @ParameterObject @PageableDefault(size = 5, sort = "name") Pageable pageable) {
        return productService.findByNameLike(new ProductName(productName), pageable);
    }

    @RateLimit("list-products")
    @GetMapping
    public Page<ProductResponseDTO> findAll(@ParameterObject @PageableDefault(size = 5, sort = "name") Pageable pageable) {
        return productService.findAll(pageable);
    }
}

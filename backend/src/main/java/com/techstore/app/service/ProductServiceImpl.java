package com.techstore.app.service;

import com.techstore.app.config.FileUploadConfig;
import com.techstore.app.domain.category.Category;
import com.techstore.app.domain.category.CategoryId;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.product.ProductId;
import com.techstore.app.domain.product.ProductName;
import com.techstore.app.domain.shared.Quantity;
import com.techstore.app.dto.product.ProductRequestDTO;
import com.techstore.app.dto.product.ProductResponseDTO;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.logger.ProductAuditLogger;
import com.techstore.app.mapper.ProductMapper;
import com.techstore.app.repository.CategoryRepository;
import com.techstore.app.repository.ProductRepository;
import com.techstore.app.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileUploadConfig fileUploadConfig;
    private final ProductAuditLogger productAuditLogger;

    @Override
    public ProductResponseDTO save(ProductRequestDTO dto, MultipartFile image) throws IOException {
        try {
            Category category = categoryRepository.findById(new CategoryId(dto.categoryId()))
                    .orElseThrow(() -> new BusinessException("Category not found"));

            Product product = ProductMapper.toEntity(dto, category, dto.stockQuantity());

            if (image != null && !image.isEmpty()) {
                String imagePath = storeProductImage(product.getId().getId().toString(), image);
                product.setImagePath(imagePath);
            }

            Product savedProduct = productRepository.save(product);
            ProductResponseDTO response = ProductMapper.toResponse(savedProduct, fileUploadConfig.getBasePath());

            productAuditLogger.logProductCreation(dto.name(), dto.categoryId().toString(), dto.price().toString(),
                    "system");

            return response;
        } catch (BusinessException e) {
            productAuditLogger.logProductCreationFailure(dto.name(), e.getMessage(), "system");
            throw e;
        }
    }

    @Override
    public List<ProductResponseDTO> findByName(ProductName productName) {
        List<Product> products = productRepository.findByName(productName);

        return products.stream()
                .map(product -> ProductMapper.toResponse(product, fileUploadConfig.getBasePath()))
                .toList();
    }

    @Override
    public Page<ProductResponseDTO> findByNameLike(ProductName productName, Pageable pageable) {
        Page<Product> products = productRepository.findByNameLike(productName.getProductName(), pageable);

        return products.map(product -> ProductMapper.toResponse(product, fileUploadConfig.getBasePath()));
    }

    @Override
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);

        return products.map(product -> ProductMapper.toResponse(product, fileUploadConfig.getBasePath()));
    }

    private String storeProductImage(String productId, MultipartFile image) throws IOException {
        validateImage(image);

        Path productDirectory = Paths.get(fileUploadConfig.getBasePath(), productId);
        Files.createDirectories(productDirectory);

        String extension = getExtension(image.getOriginalFilename());
        String safeFilename = java.util.UUID.randomUUID() + extension;
        Path targetPath = productDirectory.resolve(safeFilename).normalize();

        Files.write(targetPath, image.getBytes());
        return productId + "/" + safeFilename;
    }

    private void validateImage(MultipartFile image) throws IOException {
        if (image.getSize() > fileUploadConfig.getMaxFileSize()) {
            throw new BusinessException("Image must be smaller than 5MB.");
        }

        String filename = image.getOriginalFilename();
        String contentType = image.getContentType();
        if (filename == null || !hasAllowedExtension(filename) || !isAllowedMimeType(contentType)) {
            throw new BusinessException("File extension or MIME type not allowed. Use JPEG, PNG, or WebP.");
        }

        byte[] bytes = image.getBytes();
        if (!validateMagicBytes(bytes, contentType)) {
            throw new BusinessException("File content does not match its extension. Please upload a valid image.");
        }

        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
        if (bufferedImage == null) {
            throw new BusinessException("Failed to read image. The file may be corrupted.");
        }

        if (bufferedImage.getWidth() > fileUploadConfig.getMaxImageDimension()
                || bufferedImage.getHeight() > fileUploadConfig.getMaxImageDimension()) {
            throw new BusinessException(
                    "Image dimensions must not exceed " + fileUploadConfig.getMaxImageDimension() + "x"
                            + fileUploadConfig.getMaxImageDimension() + "px.");
        }
    }

    private boolean hasAllowedExtension(String filename) {
        String lowerFilename = filename.toLowerCase();
        return Arrays.stream(FileUploadConfig.ALLOWED_EXTENSIONS).anyMatch(lowerFilename::endsWith);
    }

    private boolean isAllowedMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        return Arrays.asList(FileUploadConfig.ALLOWED_MIME_TYPES).contains(mimeType);
    }

    private boolean validateMagicBytes(byte[] fileBytes, String mimeType) {
        if (fileBytes.length < 4) {
            return false;
        }

        return switch (mimeType) {
            case "image/jpeg" ->
                fileBytes[0] == (byte) 0xFF && fileBytes[1] == (byte) 0xD8 && fileBytes[2] == (byte) 0xFF;
            case "image/png" ->
                fileBytes[0] == (byte) 0x89 && fileBytes[1] == 0x50 && fileBytes[2] == 0x4E && fileBytes[3] == 0x47;
            case "image/webp" ->
                fileBytes[0] == 0x52 && fileBytes[1] == 0x49 && fileBytes[2] == 0x46 && fileBytes[3] == 0x46;
            default -> false;
        };
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            throw new BusinessException("File extension not found.");
        }
        return filename.substring(dotIndex).toLowerCase();
    }

    @Override
    public ProductResponseDTO updateStock(UUID productId, Integer newQuantity, String managerId) {
    try {
        if (newQuantity < 0) {
            throw new BusinessException("Stock quantity cannot be negative");
        }

        Product product = productRepository.findById(new ProductId(productId))
                .orElseThrow(() -> new BusinessException("Product not found"));

        Integer oldQuantity = product.getStockQuantity().getQuantity();
        product.updateStock(new Quantity(newQuantity));
        Product saved = productRepository.save(product);

        productAuditLogger.logStockUpdate(product.getName().getProductName(),
                oldQuantity, newQuantity, managerId);

        return ProductMapper.toResponse(saved, fileUploadConfig.getBasePath());
    } catch (Exception ex) {
        productAuditLogger.logStockUpdateFailure("unknown", ex.getMessage(), managerId);
        
        if (ex instanceof BusinessException) {
            throw ex;
        }
        throw new BusinessException("Failed to update stock: " + ex.getMessage());
    }
}
    

}

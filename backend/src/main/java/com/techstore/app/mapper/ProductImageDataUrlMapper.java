package com.techstore.app.mapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

final class ProductImageDataUrlMapper {

    private ProductImageDataUrlMapper() {
    }

    static String toDataUrl(String imagePath, String uploadBasePath) {
        if (imagePath == null || imagePath.isBlank() || uploadBasePath == null || uploadBasePath.isBlank()) {
            return null;
        }

        try {
            Path filePath = Path.of(uploadBasePath).resolve(imagePath).normalize();
            byte[] imageBytes = Files.readAllBytes(filePath);
            return "data:" + detectMimeType(imagePath) + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            return null;
        }
    }

    private static String detectMimeType(String imagePath) {
        String lowerPath = imagePath.toLowerCase();
        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lowerPath.endsWith(".png")) {
            return "image/png";
        }
        if (lowerPath.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }
}
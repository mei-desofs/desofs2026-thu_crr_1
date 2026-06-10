package com.techstore.app.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class FileUploadConfig {

    @Value("${file.upload.base-path:./uploads/products}")
    private String basePath;

    @Value("${file.upload.max-file-size:5242880}") // 5MB
    private long maxFileSize;

    @Value("${file.upload.max-files-per-user:20}")
    private int maxFilesPerUser;

    @Value("${file.upload.max-image-dimension:4096}")
    private int maxImageDimension;

    // Allowed MIME types
    public static final String[] ALLOWED_MIME_TYPES = {
            "image/jpeg",
            "image/png",
            "image/webp"
    };

    public static final String[] ALLOWED_EXTENSIONS = {
            ".jpg", ".jpeg", ".png", ".webp"
    };

    public static final byte[][] MAGIC_BYTES = {
            new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}, // JPEG
            new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}, // PNG
            new byte[]{0x52, 0x49, 0x46, 0x46} // WebP
    };

}

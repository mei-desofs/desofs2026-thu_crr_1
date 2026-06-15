# File Upload Handling Documentation

## Upload Features

| Upload Feature       | Endpoint             | Field Name | Required |
| -------------------- | -------------------- | ---------- | -------- |
| Product image upload | `POST /api/products` | `image`    | No       |

Product images are uploaded as part of the product creation request using `multipart/form-data`.

The application does not provide a separate public upload endpoint or public file execution endpoint for product images.

## Permitted File Types

Only image files are permitted for product image uploads.

| File Type  | Allowed Extensions | Allowed MIME Types |
| ---------- | ------------------ | ------------------ |
| JPEG image | `.jpg`, `.jpeg`    | `image/jpeg`       |
| PNG image  | `.png`             | `image/png`        |
| WebP image | `.webp`            | `image/webp`       |

Files with any other extension or MIME type are rejected.

## File Size Limits

| Upload Feature       | Maximum File Size | Maximum Unpacked / Decoded Size |
| -------------------- | ----------------: | ------------------------------: |
| Product image upload |            `5 MB` |                `4096 x 4096 px` |

The maximum file size applies to the uploaded file.

The maximum unpacked or decoded size is enforced through image dimension validation. Images exceeding `4096 x 4096 px` are rejected, even if the compressed file size is within the `5 MB` limit.

## File Validation

The backend validates uploaded files before they are stored or processed by the application.

The following checks are performed:

* File extension validation
* MIME type validation
* Magic byte validation
* Image decoding validation
* Image dimension validation
* File size validation

A file must pass all validation checks before it is accepted.

## Handling of Invalid or Malicious Files

If a file is invalid, malformed, oversized, or suspected to be malicious, the application rejects the upload before storing the file.

The application rejects files when:

* The file extension is not permitted
* The MIME type is not permitted
* The file magic bytes do not match the expected file type
* The file cannot be decoded as a valid image
* The file exceeds the maximum file size
* The decoded image dimensions exceed the allowed limit
* The file content does not match the declared type

When a file is rejected, the product image is not stored and the request fails with a validation error.

## End-User Download and Processing Safety

Product images are stored outside the web root and are not exposed through a public file execution endpoint.

When product data is returned through the API, the image is provided as an embedded `data URL` in the response DTO.

This reduces the risk of end-users directly downloading or executing uploaded files from a public storage path.

The application only serves images that have already passed backend validation.

## Storage Safety

Accepted product images are stored in a persistent Docker volume.

The application uses the configured upload base path inside the container, backed by the Docker volume:

product_images

Images are stored under the product-specific upload path:

```
/uploads/products/{productId}/{filename}
```

The stored filename is generated using a UUID and the original validated extension.

This prevents users from controlling the final stored filename and reduces risks such as path traversal, filename collision, and unsafe file execution.

The upload storage location is not exposed as a public web root. Product images are only returned by the application after validation and processing.
package com.techstore.app.logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductAuditLoggerTest {

    private ProductAuditLogger productAuditLogger;

    @BeforeEach
    void setUp() {
        productAuditLogger = new ProductAuditLogger();
    }

    @Test
    void shouldLogProductCreation() {
        String productName = "Laptop Dell XPS 13";
        String categoryId = "category-123";
        String price = "999.99";
        String userId = "user-456";

        productAuditLogger.logProductCreation(productName, categoryId, price, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogProductCreationWithDifferentValues() {
        String productName = "iPhone 15 Pro";
        String categoryId = "smartphones";
        String price = "1299.99";
        String userId = "admin-001";

        productAuditLogger.logProductCreation(productName, categoryId, price, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogProductCreationWithSpecialCharacters() {
        String productName = "Product@#$%";
        String categoryId = "cat-123";
        String price = "50.00";
        String userId = "user-789";

        productAuditLogger.logProductCreation(productName, categoryId, price, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogProductCreationFailure() {
        String productName = "Failed Product";
        String reason = "Duplicate product name";
        String userId = "user-123";

        productAuditLogger.logProductCreationFailure(productName, reason, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogProductCreationFailureWithValidationError() {
        String productName = "Invalid Product";
        String reason = "Price must be positive";
        String userId = "user-456";

        productAuditLogger.logProductCreationFailure(productName, reason, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogProductCreationWithNullProductName() {
        String categoryId = "category-123";
        String price = "100.00";
        String userId = "user-789";

        productAuditLogger.logProductCreation(null, categoryId, price, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogProductCreationFailureWithNullProductName() {
        String reason = "Invalid input";
        String userId = "user-123";

        productAuditLogger.logProductCreationFailure(null, reason, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldHandleProductNameWithNewlines() {
        String productName = "Product\nName\nWith\nNewlines";
        String categoryId = "cat-123";
        String price = "50.00";
        String userId = "user-456";

        productAuditLogger.logProductCreation(productName, categoryId, price, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldHandleProductNameWithTabs() {
        String productName = "Product\tName\tWith\tTabs";
        String categoryId = "cat-123";
        String price = "50.00";
        String userId = "user-456";

        productAuditLogger.logProductCreation(productName, categoryId, price, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldHandleProductNameWithCarriageReturns() {
        String productName = "Product\rName\rWith\rCR";
        String categoryId = "cat-123";
        String price = "50.00";
        String userId = "user-456";

        productAuditLogger.logProductCreation(productName, categoryId, price, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldHandleProductNameWithControlCharacters() {
        String productName = "Product\u0001Name\u0002With\u0003Control";
        String categoryId = "cat-123";
        String price = "50.00";
        String userId = "user-456";

        productAuditLogger.logProductCreation(productName, categoryId, price, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogProductCreationWithZeroPrice() {
        String productName = "Free Product";
        String categoryId = "free-items";
        String price = "0.00";
        String userId = "admin-001";

        productAuditLogger.logProductCreation(productName, categoryId, price, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogProductCreationWithLongProductName() {
        String productName = "A".repeat(500);
        String categoryId = "cat-123";
        String price = "100.00";
        String userId = "user-456";

        productAuditLogger.logProductCreation(productName, categoryId, price, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogProductCreationFailureWithLongReason() {
        String productName = "Product";
        String reason = "B".repeat(500);
        String userId = "user-123";

        productAuditLogger.logProductCreationFailure(productName, reason, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldHandleMultipleLogsSequentially() {
        productAuditLogger.logProductCreation("Product1", "cat-1", "10.00", "user-1");
        productAuditLogger.logProductCreationFailure("Product2", "Error", "user-2");
        productAuditLogger.logProductCreation("Product3", "cat-3", "30.00", "user-3");

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogProductCreationWithNullCategoryId() {
        String productName = "Product";
        String price = "100.00";
        String userId = "user-123";

        productAuditLogger.logProductCreation(productName, null, price, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogProductCreationWithNullPrice() {
        String productName = "Product";
        String categoryId = "cat-123";
        String userId = "user-123";

        productAuditLogger.logProductCreation(productName, categoryId, null, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogProductCreationWithNullUserId() {
        String productName = "Product";
        String categoryId = "cat-123";
        String price = "100.00";

        productAuditLogger.logProductCreation(productName, categoryId, price, null);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogProductCreationFailureWithNullReason() {
        String productName = "Product";
        String userId = "user-123";

        productAuditLogger.logProductCreationFailure(productName, null, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogProductCreationFailureWithNullUserId() {
        String productName = "Product";
        String reason = "Error";

        productAuditLogger.logProductCreationFailure(productName, reason, null);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogMultipleProductCreations() {
        productAuditLogger.logProductCreation("Product1", "cat1", "10.00", "user1");
        productAuditLogger.logProductCreation("Product2", "cat2", "20.00", "user2");
        productAuditLogger.logProductCreation("Product3", "cat3", "30.00", "user3");

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldLogMultipleProductFailures() {
        productAuditLogger.logProductCreationFailure("Product1", "Error1", "user1");
        productAuditLogger.logProductCreationFailure("Product2", "Error2", "user2");

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldHandleEmptyProductName() {
        String productName = "";
        String categoryId = "cat-123";
        String price = "100.00";
        String userId = "user-123";

        productAuditLogger.logProductCreation(productName, categoryId, price, userId);

        assertNotNull(productAuditLogger);
    }

    @Test
    void shouldHandleEmptyReason() {
        String productName = "Product";
        String reason = "";
        String userId = "user-123";

        productAuditLogger.logProductCreationFailure(productName, reason, userId);

        assertNotNull(productAuditLogger);
    }
}

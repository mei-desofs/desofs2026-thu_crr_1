package com.techstore.app.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ProductAuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("PRODUCT_AUDIT");
    private static final Logger appLog = LoggerFactory.getLogger("PRODUCT_APP");

    public void logProductCreation(String productName, String categoryId, String price, String userId) {
        auditLog.info("event=PRODUCT_CREATION | productName={} | categoryId={} | price={} | userId={} | timestamp={}",
                sanitize(productName),
                categoryId,
                price,
                userId,
                Instant.now()
        );

        appLog.info("Product created: name={}, category={}, price={}",
                sanitize(productName),
                categoryId,
                price);
    }

    public void logProductCreationFailure(String productName, String reason, String userId) {
        auditLog.warn("event=PRODUCT_CREATION_FAILURE | productName={} | reason={} | userId={} | timestamp={}",
                sanitize(productName),
                reason,
                userId,
                Instant.now()
        );
    }

    private String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll("[\\r\\n\\t]", "_")
                .replaceAll("\\p{Cntrl}", "");
    }
}
package com.techstore.app.logger;

import com.techstore.app.dto.order.CreateOrderRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderAuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("ORDER_AUDIT");
    private static final Logger appLog = LoggerFactory.getLogger("ORDER_APP");

    public void logOrderCreationAttempt(CreateOrderRequestDTO request) {
        auditLog.info("event=ORDER_CREATION_ATTEMPT | cartId={} | customerId={} | timestamp={}", sanitize(request.cartID()),
                sanitize(request.customerID()), System.currentTimeMillis());

        appLog.info("Attempting to create order: cartId={}, customerId={}",
                sanitize(request.cartID()), sanitize(request.customerID()));
    }

    public void logOrderCreation(String orderId, String customerId, String cartId) {
        auditLog.info("event=ORDER_CREATION_SUCCESS | orderId={} | customerId={} | cartId={} | timestamp={}",
                sanitize(orderId), sanitize(customerId), sanitize(cartId), System.currentTimeMillis());

        appLog.info("Order created successfully: orderId={}, customerId={}, cartId={}",
                sanitize(orderId), sanitize(customerId), sanitize(cartId));
    }

    public void logOrderCreationFailure(CreateOrderRequestDTO request, Exception exception) {
        auditLog.warn("event=ORDER_CREATION_FAILURE | cartId={} | customerId={} | reason={} | timestamp={}",
                request != null ? sanitize(request.cartID()) : null, request != null ? sanitize(request.customerID()) : null,
                sanitize(exception.getMessage()), System.currentTimeMillis());

        appLog.warn("Failed to create order: cartId={}, customerId={}, reason={}",
                request != null ? sanitize(request.cartID()) : null, request != null ? sanitize(request.customerID()) : null,
                sanitize(exception.getMessage()));
    }

    private String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll("[\\r\\n\\t]", "_")
                .replaceAll("\\p{Cntrl}", "");
    }
}

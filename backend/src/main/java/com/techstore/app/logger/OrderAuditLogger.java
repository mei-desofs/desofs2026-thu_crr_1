package com.techstore.app.logger;

import com.techstore.app.dto.order.CreateOrderRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderAuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("ORDER_AUDIT");
    private static final Logger appLog = LoggerFactory.getLogger("ORDER_APP");

    public void logOrderCreationAttempt(CreateOrderRequestDTO request, String userId) {
        auditLog.info("event=ORDER_CREATION_ATTEMPT | cartId={} | userId={} | timestamp={}", sanitize(request.cartID()),
                sanitize(userId), System.currentTimeMillis());

        appLog.info("Attempting to create order: cartId={}, userId={}",
                sanitize(request.cartID()), sanitize(userId));
    }

    public void logOrderCreation(String orderId, String userId, String cartId) {
        auditLog.info("event=ORDER_CREATION_SUCCESS | orderId={} | userId={} | cartId={} | timestamp={}",
                sanitize(orderId), sanitize(userId), sanitize(cartId), System.currentTimeMillis());

        appLog.info("Order created successfully: orderId={}, userId={}, cartId={}",
                sanitize(orderId), sanitize(userId), sanitize(cartId));
    }

    public void logOrderCreationFailure(CreateOrderRequestDTO request,String userId, Exception exception) {
        auditLog.warn("event=ORDER_CREATION_FAILURE | cartId={} | userId={} | reason={} | timestamp={}",
                request != null ? sanitize(request.cartID()) : null, request != null ? sanitize(userId) : null,
                sanitize(exception.getMessage()), System.currentTimeMillis());

        appLog.warn("Failed to create order: cartId={}, userId={}, reason={}",
                request != null ? sanitize(request.cartID()) : null, request != null ? sanitize(userId) : null,
                sanitize(exception.getMessage()));
    }
    public void logCustomerOrdersListingAttempt(String userId) {
        auditLog.info(
                "event=CUSTOMER_ORDER_LIST_ATTEMPT | userId={} | timestamp={}",
                sanitize(userId),
                System.currentTimeMillis()
        );

        appLog.info("Attempting to list customer orders: userId={}",
                sanitize(userId));
    }
    public void logCustomerOrdersListingSuccess(String userId, int totalOrders) {
        auditLog.info(
                "event=CUSTOMER_ORDER_LIST_SUCCESS | userId={} | totalOrders={} | timestamp={}",
                sanitize(userId),
                totalOrders,
                System.currentTimeMillis()
        );

        appLog.info("Customer orders listed successfully: userId={}, totalOrders={}",
                sanitize(userId), totalOrders);
    }

    public void logCustomerOrdersListingFailure(String userId, Exception exception) {
        auditLog.warn(
                "event=CUSTOMER_ORDER_LIST_FAILURE | userId={} | reason={} | timestamp={}",
                sanitize(userId),
                sanitize(exception.getMessage()),
                System.currentTimeMillis()
        );

        appLog.warn("Failed to list customer orders: userId={}, reason={}",
                sanitize(userId), sanitize(exception.getMessage()));
    }
    public void logCarrierOrdersListingAttempt(String userId) {
        auditLog.info(
                "event=CARRIER_ORDER_LIST_ATTEMPT | userId={} | timestamp={}",
                sanitize(userId),
                System.currentTimeMillis()
        );

        appLog.info("Attempting to list carrier orders: userId={}",
                sanitize(userId));
    }
    public void logCarrierOrdersListingSuccess(String userId, int totalOrders) {
        auditLog.info(
                "event=CARRIER_ORDER_LIST_SUCCESS | userId={} | totalOrders={} | timestamp={}",
                sanitize(userId),
                totalOrders,
                System.currentTimeMillis()
        );

        appLog.info("Carrier orders listed successfully: userId={}, totalOrders={}",
                sanitize(userId), totalOrders);
    }

    public void logCarrierOrdersListingFailure(String userId, Exception exception) {
        auditLog.warn(
                "event=CARRIER_ORDER_LIST_FAILURE | userId={} | reason={} | timestamp={}",
                sanitize(userId),
                sanitize(exception.getMessage()),
                System.currentTimeMillis()
        );

        appLog.warn("Failed to list carrier orders: userId={}, reason={}",
                sanitize(userId), sanitize(exception.getMessage()));
    }

    private String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll("[\\r\\n\\t]", "_")
                .replaceAll("\\p{Cntrl}", "");
    }

    public void logPickupAttempt(String orderId, String userId) {
        auditLog.info("event=ORDER_PICKUP_ATTEMPT | orderId={} | userId={} | timestamp={}",
                sanitize(orderId), sanitize(userId), System.currentTimeMillis());

        appLog.info("Attempting to pickup carrier order: userId={}",
                sanitize(userId));
    }

    public void logPickupSuccess(String orderId, String userId) {
        auditLog.info("event=ORDER_PICKUP_SUCCESS | orderId={} | userId={} | timestamp={}",
                sanitize(orderId), sanitize(userId), System.currentTimeMillis());

        appLog.info("Carrier order picked up successfully: userId={}, orderId={}",
                 sanitize(userId), sanitize(orderId));
    }

    public void logPickupFailure(String orderId, String userId, Exception ex) {
        auditLog.warn("event=ORDER_PICKUP_FAILURE | orderId={} | userId={} | reason={} | timestamp={}",
                sanitize(orderId), sanitize(userId), sanitize(ex.getMessage()), System.currentTimeMillis());

        appLog.warn("Failed to pickup carrier order: userId={}, reason={}",
                sanitize(userId), sanitize(ex.getMessage()));
    }
}

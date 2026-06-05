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
    public void logCustomerOrdersListingAttempt(String customerId) {
        auditLog.info(
                "event=CUSTOMER_ORDER_LIST_ATTEMPT | customerId={} | timestamp={}",
                sanitize(customerId),
                System.currentTimeMillis()
        );

        appLog.info("Attempting to list customer orders: customerId={}",
                sanitize(customerId));
    }
    public void logCustomerOrdersListingSuccess(String customerId, int totalOrders) {
        auditLog.info(
                "event=CUSTOMER_ORDER_LIST_SUCCESS | customerId={} | totalOrders={} | timestamp={}",
                sanitize(customerId),
                totalOrders,
                System.currentTimeMillis()
        );

        appLog.info("Customer orders listed successfully: customerId={}, totalOrders={}",
                sanitize(customerId), totalOrders);
    }

    public void logCustomerOrdersListingFailure(String customerId, Exception exception) {
        auditLog.warn(
                "event=CUSTOMER_ORDER_LIST_FAILURE | customerId={} | reason={} | timestamp={}",
                sanitize(customerId),
                sanitize(exception.getMessage()),
                System.currentTimeMillis()
        );

        appLog.warn("Failed to list customer orders: customerId={}, reason={}",
                sanitize(customerId), sanitize(exception.getMessage()));
    }
    public void logCarrierOrdersListingAttempt(String carrierId) {
        auditLog.info(
                "event=CARRIER_ORDER_LIST_ATTEMPT | carrierId={} | timestamp={}",
                sanitize(carrierId),
                System.currentTimeMillis()
        );

        appLog.info("Attempting to list carrier orders: customerId={}",
                sanitize(carrierId));
    }
    public void logCarrierOrdersListingSuccess(String carrierId, int totalOrders) {
        auditLog.info(
                "event=CARRIER_ORDER_LIST_SUCCESS | carrierId={} | totalOrders={} | timestamp={}",
                sanitize(carrierId),
                totalOrders,
                System.currentTimeMillis()
        );

        appLog.info("Carrier orders listed successfully: carrierId={}, totalOrders={}",
                sanitize(carrierId), totalOrders);
    }

    public void logCarrierOrdersListingFailure(String carrierId, Exception exception) {
        auditLog.warn(
                "event=CARRIER_ORDER_LIST_FAILURE | carrierId={} | reason={} | timestamp={}",
                sanitize(carrierId),
                sanitize(exception.getMessage()),
                System.currentTimeMillis()
        );

        appLog.warn("Failed to list carrier orders: carrierId={}, reason={}",
                sanitize(carrierId), sanitize(exception.getMessage()));
    }

    private String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll("[\\r\\n\\t]", "_")
                .replaceAll("\\p{Cntrl}", "");
    }

    public void logPickupAttempt(String orderId, String supabaseUserId) {
        auditLog.info("event=ORDER_PICKUP_ATTEMPT | orderId={} | supabaseUserId={} | timestamp={}",
                sanitize(orderId), sanitize(supabaseUserId), System.currentTimeMillis());

        appLog.info("Attempting to pickup carrier order: supabaseUserId={}",
                sanitize(supabaseUserId));
    }

    public void logPickupSuccess(String orderId, String supabaseUserId) {
        auditLog.info("event=ORDER_PICKUP_SUCCESS | orderId={} | supabaseUserId={} | timestamp={}",
                sanitize(orderId), sanitize(supabaseUserId), System.currentTimeMillis());

        appLog.info("Carrier order picked up successfully: supabaseUserId={}, orderId={}",
                 sanitize(supabaseUserId), sanitize(orderId));
    }

    public void logPickupFailure(String orderId, String supabaseUserId, Exception ex) {
        auditLog.warn("event=ORDER_PICKUP_FAILURE | orderId={} | supabaseUserId={} | reason={} | timestamp={}",
                sanitize(orderId), sanitize(supabaseUserId), sanitize(ex.getMessage()), System.currentTimeMillis());

        appLog.warn("Failed to pickup carrier order: supabaseUserId={}, reason={}",
                sanitize(supabaseUserId), sanitize(ex.getMessage()));
    }
}

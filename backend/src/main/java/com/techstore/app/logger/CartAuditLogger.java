package com.techstore.app.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CartAuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");
    private static final Logger appLog = LoggerFactory.getLogger("CART_APP");

    public void logCartCreation(String customerId, String cartId) {

        auditLog.info(
                "event=CART_CREATION | customerId={} | cartId={} | timestamp={}",
                sanitize(customerId),
                sanitize(cartId),
                Instant.now()
        );

        appLog.info(
                "Cart created for customer={}, cartId={}",
                sanitize(customerId),
                sanitize(cartId)
        );
    }

    public void logCartUpdate(String cartId, String productId, int quantityChange, String action) {

        auditLog.info(
                "event=CART_UPDATE | cartId={} | productId={} | quantityChange={} | action={} | timestamp={}",
                sanitize(cartId),
                sanitize(productId),
                quantityChange,
                sanitize(action),
                Instant.now()
        );

        appLog.info(
                "Cart updated: cartId={}, productId={}, change={}, action={}",
                sanitize(cartId),
                sanitize(productId),
                quantityChange,
                action
        );
    }

    public void logCartItemAdded(String cartId, String productId, int quantity) {

        auditLog.info(
                "event=CART_ITEM_ADDED | cartId={} | productId={} | quantity={} | timestamp={}",
                sanitize(cartId),
                sanitize(productId),
                quantity,
                Instant.now()
        );

        appLog.info(
                "Item added to cart: cartId={}, productId={}, quantity={}",
                sanitize(cartId),
                sanitize(productId),
                quantity
        );
    }

    public void logCartItemMerged(String cartId, String productId, int addedQuantity, int newTotalQuantity) {

        auditLog.info(
                "event=CART_ITEM_MERGED | cartId={} | productId={} | addedQuantity={} | newTotalQuantity={} | timestamp={}",
                sanitize(cartId),
                sanitize(productId),
                addedQuantity,
                newTotalQuantity,
                Instant.now()
        );

        appLog.info(
                "Cart item merged: cartId={}, productId={}, added={}, newTotal={}",
                sanitize(cartId),
                sanitize(productId),
                addedQuantity,
                newTotalQuantity
        );
    }

    public void logCartCreationFailure(String customerId, String reason) {

        auditLog.warn(
                "event=CART_CREATION_FAILURE | customerId={} | reason={} | timestamp={}",
                sanitize(customerId),
                sanitize(reason),
                Instant.now()
        );
    }

    public void logCartUpdateFailure(String cartId, String productId, String reason) {

        auditLog.warn(
                "event=CART_UPDATE_FAILURE | cartId={} | productId={} | reason={} | timestamp={}",
                sanitize(cartId),
                sanitize(productId),
                sanitize(reason),
                Instant.now()
        );
    }

    public void logCartRetrieved(String cartId, int itemCount) {
        auditLog.info(
                "event=CART_RETRIEVED | cartId={} | itemCount={} | timestamp={}",
                sanitize(cartId),
                itemCount,
                Instant.now()
        );
        appLog.info(
                "Cart retrieved: cartId={}, itemCount={}",
                sanitize(cartId),
                itemCount
        );
    }


    private String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll("[\\r\\n\\t]", "_")
                .replaceAll("\\p{Cntrl}", "");
    }
}
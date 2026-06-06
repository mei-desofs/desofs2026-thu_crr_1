package com.techstore.app.logger;

import com.techstore.app.dto.order.CreateOrderRequestDTO;
import com.techstore.app.dto.shared.AddAddressDTO;
import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderAuditLoggerTest {

    private OrderAuditLogger orderAuditLogger;

    @BeforeEach
    void setUp() {
        orderAuditLogger = new OrderAuditLogger();
    }

    @Test
    void shouldCreateOrderAuditLogger() {
        assertNotNull(orderAuditLogger);
    }

    @Test
    void shouldLogOrderCreationAttempt() {
        CreateOrderRequestDTO request = createRequest(
                "11111111-1111-1111-1111-111111111111"
        );

        assertDoesNotThrow(() -> orderAuditLogger.logOrderCreationAttempt(request,"22222222-2222-2222-2222-222222222222"));
    }

    @Test
    void shouldLogOrderCreation() {
        assertDoesNotThrow(() -> orderAuditLogger.logOrderCreation(
                "33333333-3333-3333-3333-333333333333",
                "22222222-2222-2222-2222-222222222222",
                "11111111-1111-1111-1111-111111111111"
        ));
    }

    @Test
    void shouldLogOrderCreationWithNullValues() {
        assertDoesNotThrow(() -> orderAuditLogger.logOrderCreation(
                null,
                null,
                null
        ));
    }

    @Test
    void shouldLogOrderCreationWithUnsafeCharacters() {
        assertDoesNotThrow(() -> orderAuditLogger.logOrderCreation(
                "order\n123",
                "customer\t456",
                "cart\r789"
        ));
    }

    private CreateOrderRequestDTO createRequest(String cartId) {
        return new CreateOrderRequestDTO(
                cartId,
                new AddAddressDTO(
                        "4000-001",
                        "Porto",
                        "Portugal",
                        "Rua Teste"
                )
        );
    }

    @Test
    void shouldLogOrderCreationFailure() {
        CreateOrderRequestDTO request = createRequest(
                "11111111-1111-1111-1111-111111111111"
        );

        Exception exception = new RuntimeException("Cart not found");

        assertDoesNotThrow(() -> orderAuditLogger.logOrderCreationFailure(request,"22222222-2222-2222-2222-222222222222", exception));
    }

    @Test
    void shouldLogOrderCreationFailureWithNullRequest() {
        Exception exception = new RuntimeException("Customer not found");

        assertDoesNotThrow(() -> orderAuditLogger.logOrderCreationFailure(null,null, exception));
    }

    @Test
    void shouldLogOrderCreationFailureWithUnsafeCharacters() {
        CreateOrderRequestDTO request = createRequest(
                "cart\n123"
        );

        Exception exception = new RuntimeException("Error\rwith\nunsafe\tchars");

        assertDoesNotThrow(() -> orderAuditLogger.logOrderCreationFailure(request,"customer\t456", exception));
    }
    @Test
    void shouldLogCustomerOrdersListingAttempt() {
        assertDoesNotThrow(() ->
                orderAuditLogger.logCustomerOrdersListingAttempt(
                        "customer-123"));
    }
    @Test
    void shouldLogCustomerOrdersListingAttempWithUnsafeCharacters() {

        assertDoesNotThrow(() ->
                orderAuditLogger.logCustomerOrdersListingAttempt(
                        "customer\n123"));
    }
    @Test
    void shouldLogCustomerOrdersListingSuccess() {
        assertDoesNotThrow(() ->
                orderAuditLogger.logCustomerOrdersListingSuccess(
                        "customer-123",
                        5));
    }
    @Test
    void shouldLogCustomerOrdersListingSuccessWithUnsafeCharacters() {

        assertDoesNotThrow(() ->
                orderAuditLogger.logCustomerOrdersListingSuccess(
                        "customer\n123",
                        10));
    }
    @Test
    void shouldLogCustomerOrdersListingFailure() {

        Exception ex = new RuntimeException("Database error");

        assertDoesNotThrow(() -> orderAuditLogger.logCustomerOrdersListingFailure("customer-123", ex));
    }
    @Test
    void shouldLogCustomerOrdersListingFailureWithUnsafeCharacters() {
        Exception exception = new RuntimeException("Error\rwith\nunsafe\tchars");

        assertDoesNotThrow(() ->
                orderAuditLogger.logCustomerOrdersListingFailure(
                        "customer\n123",
                        exception));
    }
    @Test
    void shouldLogCarrierOrdersListingAttempt() {
        assertDoesNotThrow(() ->
                orderAuditLogger.logCarrierOrdersListingAttempt(
                        "carrier-123"));
    }
    @Test
    void shouldLogCarrierOrdersListingAttempWithUnsafeCharacters() {

        assertDoesNotThrow(() ->
                orderAuditLogger.logCarrierOrdersListingAttempt(
                        "carrier\n123"));
    }
    @Test
    void shouldLogCarrierOrdersListingSuccess() {
        assertDoesNotThrow(() ->
                orderAuditLogger.logCarrierOrdersListingSuccess(
                        "carrier-123",
                        5));
    }
    @Test
    void shouldLogCarrierOrdersListingSuccessWithUnsafeCharacters() {

        assertDoesNotThrow(() ->
                orderAuditLogger.logCarrierOrdersListingSuccess(
                        "carrier\n123",
                        10));
    }
    @Test
    void shouldLogCarrierOrdersListingFailure() {

        Exception ex = new RuntimeException("Database error");

        assertDoesNotThrow(() -> orderAuditLogger.logCarrierOrdersListingFailure("carrier-123", ex));
    }
    @Test
    void shouldLogCarrierOrdersListingFailureWithUnsafeCharacters() {
        Exception exception = new RuntimeException("Error\rwith\nunsafe\tchars");

        assertDoesNotThrow(() ->
                orderAuditLogger.logCarrierOrdersListingFailure(
                        "carrier\n123",
                        exception));
    }
    @Test
    void shouldLogPickupAttempt() {
        assertDoesNotThrow(() ->
                orderAuditLogger.logPickupAttempt("order-123", "carrier-456"));
    }

    @Test
    void shouldLogPickupAttemptWithNullValues() {
        assertDoesNotThrow(() ->
                orderAuditLogger.logPickupAttempt(null, null));
    }

    @Test
    void shouldLogPickupAttemptWithUnsafeCharacters() {
        assertDoesNotThrow(() ->
                orderAuditLogger.logPickupAttempt("order\n123", "carrier\t456"));
    }
    @Test
    void shouldLogPickupSuccess() {
        assertDoesNotThrow(() ->
                orderAuditLogger.logPickupSuccess("order-123", "carrier-456"));
    }

    @Test
    void shouldLogPickupSuccessWithNullValues() {
        assertDoesNotThrow(() ->
                orderAuditLogger.logPickupSuccess(null, null));
    }

    @Test
    void shouldLogPickupSuccessWithUnsafeCharacters() {
        assertDoesNotThrow(() ->
                orderAuditLogger.logPickupSuccess("order\n123", "carrier\t456"));
    }
    @Test
    void shouldLogPickupFailure() {
        Exception ex = new RuntimeException("Order not found");

        assertDoesNotThrow(() ->
                orderAuditLogger.logPickupFailure("order-123", "carrier-456", ex));
    }

    @Test
    void shouldLogPickupFailureWithNullValues() {
        assertDoesNotThrow(() ->
                orderAuditLogger.logPickupFailure(null, null, new RuntimeException("err")));
    }

    @Test
    void shouldLogPickupFailureWithUnsafeCharacters() {
        Exception ex = new RuntimeException("Error\rwith\nunsafe\tchars");

        assertDoesNotThrow(() ->
                orderAuditLogger.logPickupFailure("order\n123", "carrier\t456", ex));
    }

    @Test
    void shouldLogPickupFailureWithBusinessException() {
        Exception ex = new BusinessException(
                "Order cannot be picked up: current status is PICKED_UP");

        assertDoesNotThrow(() ->
                orderAuditLogger.logPickupFailure("order-123", "carrier-456", ex));
    }
}
package com.techstore.app.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void shouldCreateErrorResponseWithAllConstructorParameters() {
        int status = 400;
        String message = "Validation failed";
        String error = "Bad Request";
        String path = "/api/customers";

        ErrorResponse errorResponse = new ErrorResponse(status, message, error, path);

        assertEquals(status, errorResponse.getStatus());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(error, errorResponse.getError());
        assertEquals(path, errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void shouldCreateErrorResponseWithThreeParameters() {
        int status = 400;
        String message = "Invalid input";
        String error = "Bad Request";

        ErrorResponse errorResponse = new ErrorResponse(status, message, error);

        assertEquals(status, errorResponse.getStatus());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(error, errorResponse.getError());
        assertNotNull(errorResponse.getTimestamp());
        assertNull(errorResponse.getPath());
    }

    @Test
    void shouldCreateErrorResponseWithAllArguments() {
        int status = 401;
        String message = "Unauthorized";
        String error = "Authentication Error";
        LocalDateTime timestamp = LocalDateTime.now();
        String path = "/api/products";
        List<String> errors = new ArrayList<>();
        errors.add("Invalid credentials");
        Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("username", "Required");

        ErrorResponse errorResponse = new ErrorResponse(
            status,
            message,
            error,
            timestamp,
            path,
            errors,
            fieldErrors
        );

        assertEquals(status, errorResponse.getStatus());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(error, errorResponse.getError());
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(path, errorResponse.getPath());
        assertEquals(errors, errorResponse.getErrors());
        assertEquals(fieldErrors, errorResponse.getFieldErrors());
    }

    @Test
    void shouldSetTimestampAutomaticallyInThreeParameterConstructor() {
        LocalDateTime beforeCreation = LocalDateTime.now();
        ErrorResponse errorResponse = new ErrorResponse(400, "Test", "Error");
        LocalDateTime afterCreation = LocalDateTime.now();

        assertNotNull(errorResponse.getTimestamp());
        assertTrue(errorResponse.getTimestamp().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(errorResponse.getTimestamp().isBefore(afterCreation.plusSeconds(1)));
    }

    @Test
    void shouldAllowSettingFieldErrors() {
        ErrorResponse errorResponse = new ErrorResponse(400, "Validation failed", "Bad Request");
        Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("email", "Invalid email format");
        fieldErrors.put("age", "Must be positive");

        errorResponse.setFieldErrors(fieldErrors);

        assertEquals(fieldErrors, errorResponse.getFieldErrors());
        assertEquals(2, errorResponse.getFieldErrors().size());
    }

    @Test
    void shouldAllowSettingErrors() {
        ErrorResponse errorResponse = new ErrorResponse(400, "Multiple errors", "Bad Request");
        List<String> errors = new ArrayList<>();
        errors.add("Error 1");
        errors.add("Error 2");

        errorResponse.setErrors(errors);

        assertEquals(errors, errorResponse.getErrors());
        assertEquals(2, errorResponse.getErrors().size());
    }

    @Test
    void shouldAllowModifyingStatus() {
        ErrorResponse errorResponse = new ErrorResponse(400, "Test", "Error");

        errorResponse.setStatus(500);

        assertEquals(500, errorResponse.getStatus());
    }

    @Test
    void shouldAllowModifyingMessage() {
        ErrorResponse errorResponse = new ErrorResponse(400, "Original", "Error");

        errorResponse.setMessage("Updated message");

        assertEquals("Updated message", errorResponse.getMessage());
    }

    @Test
    void shouldAllowModifyingError() {
        ErrorResponse errorResponse = new ErrorResponse(400, "Test", "Original Error");

        errorResponse.setError("Updated Error");

        assertEquals("Updated Error", errorResponse.getError());
    }

    @Test
    void shouldAllowModifyingPath() {
        ErrorResponse errorResponse = new ErrorResponse(400, "Test", "Error");

        errorResponse.setPath("/new/path");

        assertEquals("/new/path", errorResponse.getPath());
    }

    @Test
    void shouldHandleNullFieldErrors() {
        ErrorResponse errorResponse = new ErrorResponse(400, "Test", "Error");

        assertNull(errorResponse.getFieldErrors());

        Map<String, String> fieldErrors = new HashMap<>();
        errorResponse.setFieldErrors(fieldErrors);

        assertNotNull(errorResponse.getFieldErrors());
    }

    @Test
    void shouldHandleNullErrors() {
        ErrorResponse errorResponse = new ErrorResponse(400, "Test", "Error");

        assertNull(errorResponse.getErrors());

        List<String> errors = new ArrayList<>();
        errorResponse.setErrors(errors);

        assertNotNull(errorResponse.getErrors());
    }

    @Test
    void shouldHandleNullPath() {
        ErrorResponse errorResponse = new ErrorResponse(400, "Test", "Error");

        assertNull(errorResponse.getPath());
    }
}

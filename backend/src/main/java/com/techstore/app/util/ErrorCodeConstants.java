package com.techstore.app.util;

/**
 * Centralized error codes for the application.
 * These codes are used internally for logging, auditing, and metrics.
 * They are NOT exposed to API clients (only generic messages are sent).
 */
public final class ErrorCodeConstants {

    // Authentication & Authorization Errors
    public static final String AUTH_DUPLICATE_EMAIL = "AUTH_DUPLICATE_EMAIL";
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_INVALID_CREDENTIALS";
    public static final String AUTH_INVALID_PASSWORD = "AUTH_INVALID_PASSWORD";
    public static final String AUTH_INVALID_EMAIL = "AUTH_INVALID_EMAIL";
    public static final String AUTH_INVALID_TOKEN = "AUTH_INVALID_TOKEN";
    public static final String AUTH_FAILED = "AUTH_FAILED";
    public static final String AUTH_SERVICE_ERROR = "AUTH_SERVICE_ERROR";
    public static final String AUTH_CALLBACK_ERROR = "AUTH_CALLBACK_ERROR";

    // Rate Limiting
    public static final String RATE_LIMIT = "RATE_LIMIT";

    // Validation Errors
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";

    // Constraint Violations
    public static final String CONSTRAINT_VIOLATION = "CONSTRAINT_VIOLATION";
    public static final String CONCURRENT_MODIFICATION = "CONCURRENT_MODIFICATION";

    // System/Internal Errors
    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";

    private ErrorCodeConstants() {
    }
}

package com.techstore.app.exception;

public class RateLimitException extends RuntimeException {

    public RateLimitException() {
        super("Too many requests. Please wait a moment and try again.");
    }

    public RateLimitException(String message) {
        super(message);
    }
}

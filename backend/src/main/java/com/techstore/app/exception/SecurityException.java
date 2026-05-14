package com.techstore.app.exception;

public class SecurityException extends RuntimeException {

    private final String code;

    public SecurityException(String message, String code) {
        super(message);
        this.code = code;
    }

    public SecurityException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

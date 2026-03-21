package com.techstore.app.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Getter
@Setter
public class ErrorResponse {

    private int status;
    private String message;
    private String error;
    private LocalDateTime timestamp;
    private String path;
    private List<String> errors;
    private Map<String, String> fieldErrors;

    public ErrorResponse(int status, String message, String error) {
        this.status = status;
        this.message = message;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String message, String error, String path) {
        this(status, message, error);
        this.path = path;
    }
}


package com.techstore.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void shouldHandleBusinessException() {
        String exceptionMessage = "Invalid cart operation";
        BusinessException exception = new BusinessException(exceptionMessage);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, mockRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals(exceptionMessage, response.getBody().getMessage());
        assertEquals("Business Logic Error", response.getBody().getError());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleValidationException() {
        String exceptionMessage = "Validation failed for field";
        ValidationException exception = new ValidationException(exceptionMessage);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleJakartaValidationException(exception, mockRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals(exceptionMessage, response.getBody().getMessage());
        assertEquals("Validation Error", response.getBody().getError());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldIncludeRequestPathInErrorResponse() {
        String expectedPath = "/api/customers/123";
        when(mockRequest.getRequestURI()).thenReturn(expectedPath);
        BusinessException exception = new BusinessException("Test error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, mockRequest);

        assertEquals(expectedPath, response.getBody().getPath());
    }

    @Test
    void shouldIncludeTimestampInErrorResponse() {
        BusinessException exception = new BusinessException("Test error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, mockRequest);

        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldReturnBadRequestStatusForBusinessException() {
        BusinessException exception = new BusinessException("Test");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }

    @Test
    void shouldReturnBadRequestStatusForValidationException() {
        ValidationException exception = new ValidationException("Test");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleJakartaValidationException(exception, mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }

    @Test
    void shouldPreserveExceptionMessageInResponse() {
        String customMessage = "Custom business logic error";
        BusinessException exception = new BusinessException(customMessage);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, mockRequest);

        assertEquals(customMessage, response.getBody().getMessage());
    }

    @Test
    void shouldSetCorrectErrorTypeForBusinessException() {
        BusinessException exception = new BusinessException("Test");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, mockRequest);

        assertEquals("Business Logic Error", response.getBody().getError());
    }

    @Test
    void shouldSetCorrectErrorTypeForValidationException() {
        ValidationException exception = new ValidationException("Test");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleJakartaValidationException(exception, mockRequest);

        assertEquals("Validation Error", response.getBody().getError());
    }

    @Test
    void shouldHandleMultipleBusinessExceptions() {
        BusinessException exception1 = new BusinessException("Error 1");
        BusinessException exception2 = new BusinessException("Error 2");

        ResponseEntity<ErrorResponse> response1 = exceptionHandler.handleBusinessException(exception1, mockRequest);
        ResponseEntity<ErrorResponse> response2 = exceptionHandler.handleBusinessException(exception2, mockRequest);

        assertNotEquals(response1.getBody().getMessage(), response2.getBody().getMessage());
        assertEquals("Error 1", response1.getBody().getMessage());
        assertEquals("Error 2", response2.getBody().getMessage());
    }

    @Test
    void shouldHandleDataIntegrityViolationException() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Unique constraint violation");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception, mockRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Constraint Violation", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("constraint violation"));
    }

    @Test
    void shouldHandleConstraintViolationException() {
        ConstraintViolationException exception = mock(ConstraintViolationException.class);
        when(exception.getMessage()).thenReturn("FK constraint violation");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolation(exception, mockRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Constraint Violation", response.getBody().getError());
    }

    @Test
    void shouldHandleStaleObjectStateException() {
        StaleObjectStateException exception = mock(StaleObjectStateException.class);
        when(exception.getMessage()).thenReturn("Concurrent modification");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleStaleObjectState(exception, mockRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
        assertEquals("Concurrent Modification", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("modified by another user"));
    }

    @Test
    void shouldHandleAccessDeniedException() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(exception, mockRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().getStatus());
        assertEquals("Access Denied", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("permission"));
    }

    @Test
    void shouldHandleAuthenticationException() {
        AuthenticationException exception = new AuthenticationException("Authentication failed") {};

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(exception, mockRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("Authentication"));
    }

    @Test
    void shouldHandleBadCredentialsException() {
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentials(exception, mockRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("Invalid username or password"));
    }

    @Test
    void shouldHandleInternalAuthenticationServiceException() {
        InternalAuthenticationServiceException exception = new InternalAuthenticationServiceException("Service error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInternalAuthenticationService(exception, mockRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("service error"));
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception, mockRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    void shouldHandleIllegalArgumentExceptionWithNullMessage() {
        IllegalArgumentException exception = new IllegalArgumentException();

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception, mockRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Invalid request data"));
    }

    @Test
    void shouldHandleNumberFormatException() {
        NumberFormatException exception = new NumberFormatException("Invalid number");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNumberFormatException(exception, mockRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("numeric value"));
    }

    @Test
    void shouldHandleGenericException() {
        Exception exception = new Exception("Unexpected error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(exception, mockRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("unexpected error"));
    }

    @Test
    void shouldIncludePathInAllExceptionResponses() {
        String expectedPath = "/api/users/register";
        when(mockRequest.getRequestURI()).thenReturn(expectedPath);
        
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Test");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception, mockRequest);

        assertEquals(expectedPath, response.getBody().getPath());
    }

    @Test
    void shouldReturnConflictStatusForStaleObjectStateException() {
        StaleObjectStateException exception = mock(StaleObjectStateException.class);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleStaleObjectState(exception, mockRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    void shouldReturnForbiddenStatusForAccessDeniedException() {
        AccessDeniedException exception = new AccessDeniedException("Forbidden");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(exception, mockRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    void shouldReturnUnauthorizedStatusForAuthenticationException() {
        AuthenticationException exception = new AuthenticationException("Unauthorized") {};

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(exception, mockRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    void shouldReturnInternalServerErrorStatusForGenericException() {
        Exception exception = new Exception("Generic error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(exception, mockRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
    }
}

package com.techstore.app.logger;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthAuditLoggerTest {

    private AuthAuditLogger authAuditLogger;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        authAuditLogger = new AuthAuditLogger();
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRemoteAddr()).thenReturn("192.168.1.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
    }

    @Test
    void shouldLogSuccessfulLoginAttempt() {
        String email = "user@example.com";
        
        authAuditLogger.logLoginAttempt(email, true, mockRequest);
        
        verify(mockRequest).getRemoteAddr();
        verify(mockRequest).getHeader("User-Agent");
    }

    @Test
    void shouldLogFailedLoginAttempt() {
        String email = "user@example.com";
        
        authAuditLogger.logLoginAttempt(email, false, mockRequest);
        
        verify(mockRequest).getRemoteAddr();
        verify(mockRequest).getHeader("User-Agent");
    }

    @Test
    void shouldLogTokenRefreshSuccess() {
        String userId = "user-123";
        
        authAuditLogger.logTokenRefresh(userId, true, mockRequest);
        
        verify(mockRequest).getRemoteAddr();
    }

    @Test
    void shouldLogTokenRefreshFailure() {
        String userId = "user-123";
        
        authAuditLogger.logTokenRefresh(userId, false, mockRequest);
        
        verify(mockRequest).getRemoteAddr();
    }

    @Test
    void shouldHandleNullUserAgent() {
        when(mockRequest.getHeader("User-Agent")).thenReturn(null);
        String email = "user@example.com";
        
        authAuditLogger.logLoginAttempt(email, true, mockRequest);
        
        verify(mockRequest).getHeader("User-Agent");
    }

    @Test
    void shouldHandleEmptyEmail() {
        String email = "";
        
        authAuditLogger.logLoginAttempt(email, true, mockRequest);
        
        verify(mockRequest).getRemoteAddr();
    }

    @Test
    void shouldHandleNullEmail() {
        authAuditLogger.logLoginAttempt(null, true, mockRequest);
        
        verify(mockRequest).getRemoteAddr();
    }

    @Test
    void shouldHandleEmptyUserId() {
        String userId = "";
        
        authAuditLogger.logTokenRefresh(userId, true, mockRequest);
        
        verify(mockRequest).getRemoteAddr();
    }

    @Test
    void shouldHandleNullUserId() {
        authAuditLogger.logTokenRefresh(null, true, mockRequest);
        
        verify(mockRequest).getRemoteAddr();
    }

    @Test
    void shouldHandleLongUserAgent() {
        String longUserAgent = "A".repeat(500);
        when(mockRequest.getHeader("User-Agent")).thenReturn(longUserAgent);
        String email = "user@example.com";
        
        authAuditLogger.logLoginAttempt(email, true, mockRequest);
        
        verify(mockRequest).getHeader("User-Agent");
    }

    @Test
    void shouldLogLoginAttemptWithDifferentEmails() {
        authAuditLogger.logLoginAttempt("admin@example.com", true, mockRequest);
        authAuditLogger.logLoginAttempt("user@test.com", false, mockRequest);
        
        verify(mockRequest, times(2)).getRemoteAddr();
    }

    @Test
    void shouldLogTokenRefreshMultipleTimes() {
        authAuditLogger.logTokenRefresh("user-1", true, mockRequest);
        authAuditLogger.logTokenRefresh("user-2", false, mockRequest);
        authAuditLogger.logTokenRefresh("user-3", true, mockRequest);
        
        verify(mockRequest, times(3)).getRemoteAddr();
    }

    @Test
    void shouldLogLoginAttemptWithSpecialCharacters() {
        String email = "user+tag@example.com";
        
        authAuditLogger.logLoginAttempt(email, true, mockRequest);
        
        verify(mockRequest).getRemoteAddr();
    }

    @Test
    void shouldLogTokenRefreshWithSpecialCharacters() {
        String userId = "user@domain-123_456";
        
        authAuditLogger.logTokenRefresh(userId, true, mockRequest);
        
        verify(mockRequest).getRemoteAddr();
    }

    @Test
    void shouldHandleDifferentRemoteAddresses() {
        when(mockRequest.getRemoteAddr()).thenReturn("10.0.0.1");
        authAuditLogger.logLoginAttempt("user@example.com", true, mockRequest);
        
        verify(mockRequest).getRemoteAddr();
        
        when(mockRequest.getRemoteAddr()).thenReturn("192.168.0.1");
        authAuditLogger.logTokenRefresh("user-123", true, mockRequest);
        
        verify(mockRequest, times(2)).getRemoteAddr();
    }
}

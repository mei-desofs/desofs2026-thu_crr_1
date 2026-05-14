package com.techstore.app.exception;

import com.techstore.app.util.ErrorCodeConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;


public class ExceptionToSafeResponseMapper {

    private static final String GENERIC_MESSAGE = "Request could not be processed";

    public static ErrorResponse mapBusinessException(BusinessException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                GENERIC_MESSAGE,
                "Bad Request",
                request.getRequestURI());
        response.setCode(ex.getCode());

        return response;
    }

    public static ErrorResponse mapSecurityException(SecurityException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                GENERIC_MESSAGE,
                "Bad Request",
                request.getRequestURI());
        response.setCode(ex.getCode());

        return response;
    }

    public static ErrorResponse mapAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                GENERIC_MESSAGE,
                "Unauthorized",
                request.getRequestURI());
        response.setCode(ErrorCodeConstants.AUTH_FAILED);

        return response;
    }

    public static ErrorResponse mapBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                GENERIC_MESSAGE,
                "Unauthorized",
                request.getRequestURI());
        response.setCode(ErrorCodeConstants.AUTH_INVALID_CREDENTIALS);

        return response;
    }

    public static ErrorResponse mapInternalAuthenticationServiceException(
            InternalAuthenticationServiceException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                GENERIC_MESSAGE,
                "Unauthorized",
                request.getRequestURI());
        response.setCode(ErrorCodeConstants.AUTH_SERVICE_ERROR);

        return response;
    }

    public static ErrorResponse mapAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "You do not have permission to access this resource.",
                "Access Denied",
                request.getRequestURI());
        response.setCode(ErrorCodeConstants.ACCESS_DENIED);

        return response;
    }

    public static ErrorResponse mapIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                GENERIC_MESSAGE,
                "Bad Request",
                request.getRequestURI());
        response.setCode(ErrorCodeConstants.INVALID_REQUEST);

        return response;
    }

    public static ErrorResponse mapGenericException(Exception ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Please try again later.",
                "Internal Server Error",
                request.getRequestURI());
        response.setCode(ErrorCodeConstants.SYSTEM_ERROR);

        return response;
    }
}

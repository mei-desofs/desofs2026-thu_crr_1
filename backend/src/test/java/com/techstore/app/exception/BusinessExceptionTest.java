package com.techstore.app.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void shouldCreateBusinessExceptionWithMessage() {
        String message = "Invalid operation";

        BusinessException exception = new BusinessException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldThrowBusinessException() {
        assertThrows(BusinessException.class, () -> {
            throw new BusinessException("Test exception");
        });
    }

    @Test
    void shouldCatchBusinessExceptionAsRuntimeException() {
        assertThrows(RuntimeException.class, () -> {
            throw new BusinessException("Test exception");
        });
    }

    @Test
    void shouldPreserveExceptionMessage() {
        String expectedMessage = "Value is null or zero or negative";

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            throw new BusinessException(expectedMessage);
        });

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldAllowNullMessage() {
        BusinessException exception = new BusinessException(null);

        assertNull(exception.getMessage());
    }

    @Test
    void shouldAllowEmptyMessage() {
        String emptyMessage = "";

        BusinessException exception = new BusinessException(emptyMessage);

        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void shouldContainMessageInToString() {
        String message = "Cart ID cannot be null";

        BusinessException exception = new BusinessException(message);

        assertTrue(exception.toString().contains(message));
    }
}

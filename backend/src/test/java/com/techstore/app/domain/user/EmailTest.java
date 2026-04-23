package com.techstore.app.domain.user;

import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EmailTest {

    @Test
    void ensureValidEmailIsAccepted() {
        // Arrange
        String validEmail = "test@gmail.com";

        // Act & Assert
        assertDoesNotThrow(() -> new Email(validEmail));
    }

    @Test
    void ensureInvalidEmailThrowsException() {
        // Arrange
        String invalidEmail = "invalid-email";

        // Act
        BusinessException exception = assertThrows(BusinessException.class,
                () -> new Email(invalidEmail));

        assertTrue(exception.getMessage().contains("Invalid email format. Email must contain an '@' symbol and a valid domain."));
    }

    @Test
    void ensureNullEmailThrowsException() {
        // Act
        BusinessException exception = assertThrows(BusinessException.class,
                () -> new Email(null));

        // Assert
        assertTrue(exception.getMessage().contains("Invalid email format. Email must contain an '@' symbol and a valid domain."));
    }
}

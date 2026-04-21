package com.techstore.app.domain.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Email(invalidEmail));

        assertTrue(exception.getMessage().contains("Invalid email format. Email must contain an '@' symbol and a valid domain."));
    }

    @Test
    void ensureNullEmailThrowsException() {
        // Act
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Email(null));

        // Assert
        assertTrue(exception.getMessage().contains("Invalid email format. Email must contain an '@' symbol and a valid domain."));
    }
}

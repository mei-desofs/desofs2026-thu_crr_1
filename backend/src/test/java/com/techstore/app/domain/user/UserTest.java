package com.techstore.app.domain.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class UserTest {

    @Test
    void ensureNewUserStartsWithExpectedDefaults() {
        // Arrange & Act
        User user = new User();

        // Assert
        assertNull(user.getId());
        assertNull(user.getVersion());
        assertNull(user.getEmail());
        assertNull(user.getRole());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }

    @Test
    void ensureSettersPersistAssignedValues() {
        // Arrange
        User user = new User();
        Email email = mock(Email.class);

        // Act
        user.setEmail(email);
        user.setRole(Role.CUSTOMER);

        // Assert
        assertEquals(email, user.getEmail());
        assertEquals(Role.CUSTOMER, user.getRole());
    }

    @Test
    void ensureOnCreateSetsCreatedAndUpdatedAt() {
        // Arrange
        User user = new User();

        // Act
        user.onCreate();

        // Assert
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
    }

    @Test
    void ensureOnUpdateRefreshesOnlyUpdatedAt() {
        // Arrange
        User user = new User();
        user.onCreate();
        LocalDateTime createdAt = user.getCreatedAt();
        LocalDateTime firstUpdatedAt = user.getUpdatedAt();

        // Act
        user.onUpdate();

        // Assert
        assertEquals(createdAt, user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertFalse(user.getUpdatedAt().isBefore(firstUpdatedAt));
    }
}

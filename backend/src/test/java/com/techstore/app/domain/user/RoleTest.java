package com.techstore.app.domain.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoleTest {

    @Test
    void ensureFromStringReturnsExpectedRole() {
        // Arrange
        String roleName = "MANAGER";

        // Act
        Role role = Role.fromString(roleName);

        // Assert
        assertEquals(Role.MANAGER, role);
        assertEquals(Role.MANAGER.getDescription(), role.getDescription());
    }

    @Test
    void ensureFromStringIsCaseInsensitive() {
        // Arrange
        String roleName = "customer";

        // Act
        Role role = Role.fromString(roleName);

        // Assert
        assertEquals(Role.CUSTOMER, role);
        assertEquals(Role.CUSTOMER.getDescription(), role.getDescription());
    }

    @Test
    void ensureFromStringWithInvalidRoleThrowsException() {
        // Arrange
        String invalidRoleName = "INVALID_ROLE";

        // Act
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> Role.fromString(invalidRoleName));

        // Assert
        assertEquals("Invalid role: " + invalidRoleName, exception.getMessage());
    }

    @Test
    void ensureFromStringWithNullRoleThrowsException() {
        // Act
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> Role.fromString(null));

        // Assert
        assertEquals("Role cannot be null", exception.getMessage());
    }
}

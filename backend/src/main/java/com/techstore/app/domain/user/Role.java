package com.techstore.app.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Enum representing user roles in the system.
 */
@AllArgsConstructor
@Getter
public enum Role {
    CUSTOMER("Customer"),
    MANAGER("Manager"),
    CARRIER("Carrier");

    private String description;

    public static Role fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        return Arrays.stream(Role.values())
                .filter(role -> role.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + value));
    }
}

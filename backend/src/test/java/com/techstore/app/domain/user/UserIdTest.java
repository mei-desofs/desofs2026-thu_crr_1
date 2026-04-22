package com.techstore.app.domain.user;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserIdTest {

    @Test
    void shouldCreateUserIdWithUuid() {
        UUID uuid = UUID.randomUUID();

        UserId userId = new UserId(uuid);

        assertEquals(uuid, userId.getId());
    }

    @Test
    void shouldThrowExceptionWhenUuidIsNull() {
        assertThrows(NullPointerException.class, () -> new UserId(null));
    }

    @Test
    void shouldCreateNewRandomUserId() {
        UserId userId = UserId.newId();

        assertNotNull(userId);
        assertNotNull(userId.getId());
    }

    @Test
    void shouldCreateUserIdFromString() {
        UUID uuid = UUID.randomUUID();

        UserId userId = UserId.fromString(uuid.toString());

        assertEquals(uuid, userId.getId());
    }

    @Test
    void shouldThrowExceptionWhenStringIsInvalidUuid() {
        assertThrows(IllegalArgumentException.class, () -> UserId.fromString("invalid-uuid"));
    }
}

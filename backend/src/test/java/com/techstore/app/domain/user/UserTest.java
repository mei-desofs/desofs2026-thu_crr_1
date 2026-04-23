package com.techstore.app.domain.user;

import org.junit.jupiter.api.Test;
import com.techstore.app.exception.BusinessException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void ensureDefaultConstructorLeavesFieldsNull() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getVersion());
        assertNull(user.getEmail());
        assertNull(user.getRole());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }

    @Test
    void ensureConstructorInitializesProvidedFields() {
        Email email = new Email("test@example.com");
        Role role = Role.CUSTOMER;
        SupabaseUserId supId = SupabaseUserId.newId();

        User user = new User(email, role, supId);

        assertNull(user.getId());
        assertNull(user.getVersion());
        assertEquals(email, user.getEmail());
        assertEquals(role, user.getRole());
        assertEquals(supId, user.getSupabaseUserId());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }

    @Test
    void ensureOnCreateSetsCreatedAndUpdatedAt() {
        User user = new User();

        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
    }

    @Test
    void ensureOnUpdateRefreshesOnlyUpdatedAt() {
        User user = new User();
        user.onCreate();
        LocalDateTime createdAt = user.getCreatedAt();
        LocalDateTime firstUpdatedAt = user.getUpdatedAt();

        user.onUpdate();
        // Assert
        assertEquals(createdAt, user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertFalse(user.getUpdatedAt().isBefore(firstUpdatedAt));
    }

    @Test
    void ensureConstructorRejectsNullEmail() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> new User(null, Role.CUSTOMER, SupabaseUserId.newId()));

        assertEquals("Email cannot be null.", exception.getMessage());
    }

    @Test
    void ensureConstructorRejectsNullRole() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> new User(new Email("a@b.com"), null, SupabaseUserId.newId()));

        assertEquals("Role cannot be null.", exception.getMessage());
    }

    @Test
    void ensureConstructorRejectsNullSupabaseUserId() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> new User(new Email("a@b.com"), Role.CUSTOMER, null));

        assertEquals("Supabase User ID cannot be null.", exception.getMessage());
    }
}

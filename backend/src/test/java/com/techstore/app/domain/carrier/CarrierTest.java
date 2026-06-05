package com.techstore.app.domain.carrier;

import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.customer.Nif;
import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.Role;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.domain.user.User;
import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CarrierTest {

    @Test
    void ensureDefaultConstructorLeavesFieldsNull() {
        Carrier carrier = new Carrier();

        assertNull(carrier.getId());
        assertNull(carrier.getUser());
        assertNull(carrier.getCreatedAt());
        assertNull(carrier.getUpdatedAt());
    }

    @Test
    void ensureConstructorInitializesProvidedFields() {
        User user = new User(new Email("carrier@example.com"), Role.CARRIER, SupabaseUserId.newId());

        Carrier carrier = new Carrier(user);

        assertNotNull(carrier.getId());
        assertEquals(user, carrier.getUser());
        assertNull(carrier.getCreatedAt());
        assertNull(carrier.getUpdatedAt());
    }

    @Test
    void ensureConstructorRejectsNullUser() {
        BusinessException exception = assertThrows(BusinessException.class, () -> new Carrier(null));

        assertEquals("User cannot be null.", exception.getMessage());
    }

    @Test
    void ensureOnCreateSetsCreatedAndUpdatedAt() {
        Carrier carrier = new Carrier();

        carrier.onCreate();

        assertNotNull(carrier.getCreatedAt());
        assertNotNull(carrier.getUpdatedAt());
        assertEquals(carrier.getCreatedAt(), carrier.getUpdatedAt());
    }

    @Test
    void ensureOnUpdateRefreshesOnlyUpdatedAt() {
        Carrier carrier = new Carrier();
        carrier.onCreate();
        LocalDateTime createdAt = carrier.getCreatedAt();
        LocalDateTime firstUpdatedAt = carrier.getUpdatedAt();

        carrier.onUpdate();

        assertEquals(createdAt, carrier.getCreatedAt());
        assertNotNull(carrier.getUpdatedAt());
        assertFalse(carrier.getUpdatedAt().isBefore(firstUpdatedAt));
    }
}

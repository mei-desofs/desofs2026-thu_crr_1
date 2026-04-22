package com.techstore.app.domain.customer;

import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.Role;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.domain.user.User;
import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerTest {

    @Test
    void ensureDefaultConstructorLeavesFieldsNull() {
        Customer customer = new Customer();

        assertNull(customer.getId());
        assertNull(customer.getNif());
        assertNull(customer.getUser());
        assertNull(customer.getCreatedAt());
        assertNull(customer.getUpdatedAt());
    }

    @Test
    void ensureConstructorInitializesProvidedFields() {
        Nif nif = new Nif("123456789");
        User user = new User(new Email("customer@example.com"), Role.CUSTOMER, SupabaseUserId.newId());

        Customer customer = new Customer(nif, user);

        assertNotNull(customer.getId());
        assertEquals(nif, customer.getNif());
        assertEquals(user, customer.getUser());
        assertNull(customer.getCreatedAt());
        assertNull(customer.getUpdatedAt());
    }

    @Test
    void ensureConstructorRejectsNullNif() {
        User user = new User(new Email("customer@example.com"), Role.CUSTOMER, SupabaseUserId.newId());
        BusinessException exception = assertThrows(BusinessException.class, () -> new Customer(null, user));

        assertEquals("NIF cannot be null.", exception.getMessage());
    }

    @Test
    void ensureConstructorRejectsNullUser() {
        Nif nif = new Nif("123456789");
        BusinessException exception = assertThrows(BusinessException.class, () -> new Customer(nif, null));

        assertEquals("User cannot be null.", exception.getMessage());
    }

    @Test
    void ensureOnCreateSetsCreatedAndUpdatedAt() {
        Customer customer = new Customer();

        customer.onCreate();

        assertNotNull(customer.getCreatedAt());
        assertNotNull(customer.getUpdatedAt());
        assertEquals(customer.getCreatedAt(), customer.getUpdatedAt());
    }

    @Test
    void ensureOnUpdateRefreshesOnlyUpdatedAt() {
        Customer customer = new Customer();
        customer.onCreate();
        LocalDateTime createdAt = customer.getCreatedAt();
        LocalDateTime firstUpdatedAt = customer.getUpdatedAt();

        customer.onUpdate();

        assertEquals(createdAt, customer.getCreatedAt());
        assertNotNull(customer.getUpdatedAt());
        assertFalse(customer.getUpdatedAt().isBefore(firstUpdatedAt));
    }
}

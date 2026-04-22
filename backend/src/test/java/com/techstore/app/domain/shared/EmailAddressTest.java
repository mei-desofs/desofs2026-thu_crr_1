package com.techstore.app.domain.shared;

import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EmailAddressTest {

    @Test
    void shouldCreateEmailWithValidValue() {
        String email = "test@example.com";

        EmailAddress emailAddress = new EmailAddress(email);

        assertEquals(email, emailAddress.getEmail());
    }

    @Test
    void shouldCreateEmailWithComplexValidFormat() {
        String email = "user.name+tag@domain.co.uk";

        EmailAddress emailAddress = new EmailAddress(email);

        assertEquals(email, emailAddress.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        assertThrows(BusinessException.class, () -> new EmailAddress(null));
    }

    @Test
    void shouldThrowExceptionWhenEmailHasNoAtSymbol() {
        assertThrows(BusinessException.class, () -> new EmailAddress("invalidemail.com"));
    }

    @Test
    void shouldThrowExceptionWhenEmailHasNoDomain() {
        assertThrows(BusinessException.class, () -> new EmailAddress("test@"));
    }

    @Test
    void shouldThrowExceptionWhenEmailHasNoTopLevelDomain() {
        assertThrows(BusinessException.class, () -> new EmailAddress("a@b"));
    }

    @Test
    void shouldThrowExceptionWhenEmailHasInvalidCharacters() {
        assertThrows(BusinessException.class, () -> new EmailAddress("test@exa mple.com"));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        assertThrows(BusinessException.class, () -> new EmailAddress(""));
    }
}
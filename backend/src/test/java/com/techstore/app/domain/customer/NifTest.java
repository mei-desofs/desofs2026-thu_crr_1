package com.techstore.app.domain.customer;

import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NifTest {

    @Test
    void shouldCreateNifWhenValueIsValid() {
        String validNif = "123456789";

        Nif nif = new Nif(validNif);

        assertEquals(validNif, nif.getValue());
    }

    @Test
    void shouldThrowExceptionWhenNifIsNull() {
        BusinessException exception = assertThrows(BusinessException.class, () -> new Nif(null));

        assertEquals("Invalid Portuguese NIF.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNifHasInvalidFormat() {
        BusinessException exception = assertThrows(BusinessException.class, () -> new Nif("12345ABC9"));

        assertEquals("Invalid Portuguese NIF.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNifHasInvalidChecksum() {
        BusinessException exception = assertThrows(BusinessException.class, () -> new Nif("123456780"));

        assertEquals("Invalid Portuguese NIF.", exception.getMessage());
    }

    @Test
    void shouldReturnTrueForValidNif() {
        assertTrue(Nif.isValid("123456789"));
    }

    @Test
    void shouldReturnFalseForNifWithInvalidPrefix() {
        assertFalse(Nif.isValid("423456789"));
    }

    @Test
    void shouldReturnFalseForNifWithInvalidChecksum() {
        assertFalse(Nif.isValid("123456780"));
    }

    @Test
    void shouldReturnFalseForNullNif() {
        assertFalse(Nif.isValid(null));
    }
}

package com.techstore.app.domain.shared;

import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AddressTest {

    @Test
    void shouldCreateAddressWithValidData() {
        Address address = new Address(
                "4000-123",
                "Porto",
                "Portugal",
                "Rua de Santa Catarina"
        );

        assertEquals("4000-123", address.getPostalCode());
        assertEquals("Porto", address.getCity());
        assertEquals("Portugal", address.getCountry());
        assertEquals("Rua de Santa Catarina", address.getStreet());
    }

    @Test
    void shouldThrowExceptionWhenPostalCodeIsNull() {
        assertThrows(BusinessException.class, () ->
                new Address(null, "Porto", "Portugal", "Rua X"));
    }

    @Test
    void shouldThrowExceptionWhenPostalCodeIsInvalid() {
        assertThrows(BusinessException.class, () ->
                new Address("4000123", "Porto", "Portugal", "Rua X"));
    }

    @Test
    void shouldThrowExceptionWhenPostalCodeHasWrongFormat() {
        assertThrows(BusinessException.class, () ->
                new Address("40-123", "Porto", "Portugal", "Rua X"));
    }

    @Test
    void shouldThrowExceptionWhenCityIsNull() {
        assertThrows(BusinessException.class, () ->
                new Address("4000-123", null, "Portugal", "Rua X"));
    }

    @Test
    void shouldThrowExceptionWhenCityIsBlank() {
        assertThrows(BusinessException.class, () ->
                new Address("4000-123", "   ", "Portugal", "Rua X"));
    }

    @Test
    void shouldThrowExceptionWhenCountryIsNull() {
        assertThrows(BusinessException.class, () ->
                new Address("4000-123", "Porto", null, "Rua X"));
    }

    @Test
    void shouldThrowExceptionWhenCountryIsBlank() {
        assertThrows(BusinessException.class, () ->
                new Address("4000-123", "Porto", "   ", "Rua X"));
    }

    @Test
    void shouldThrowExceptionWhenStreetIsNull() {
        assertThrows(BusinessException.class, () ->
                new Address("4000-123", "Porto", "Portugal", null));
    }

    @Test
    void shouldThrowExceptionWhenStreetIsBlank() {
        assertThrows(BusinessException.class, () ->
                new Address("4000-123", "Porto", "Portugal", "   "));
    }
}
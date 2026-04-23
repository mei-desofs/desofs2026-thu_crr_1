package com.techstore.app.domain.customer;

import com.techstore.app.exception.BusinessException;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Embeddable
public class Nif {

    private String value;

    protected Nif() {
        // For JPA
    }

    public Nif(String value) {
        if (!isValid(value)) {
            throw new BusinessException("Invalid Portuguese NIF.");
        }
        this.value = value;
    }

    // Validates the Portuguese NIF
    public static boolean isValid(String value) {
        if (value == null || !value.matches("\\d{9}")) {
            return false;
        }

        char firstDigit = value.charAt(0);
        if ("1235689".indexOf(firstDigit) == -1) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 8; i++) {
            int digit = Character.getNumericValue(value.charAt(i));
            int weight = 9 - i;
            sum += digit * weight;
        }

        int remainder = sum % 11;
        int checkDigit = (remainder < 2) ? 0 : 11 - remainder;
        int providedCheckDigit = Character.getNumericValue(value.charAt(8));

        return checkDigit == providedCheckDigit;
    }
}

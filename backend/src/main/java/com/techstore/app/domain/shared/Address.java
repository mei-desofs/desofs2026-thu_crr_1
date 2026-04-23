package com.techstore.app.domain.shared;

import com.techstore.app.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@EqualsAndHashCode
@Getter
public class Address {

    @NotBlank
    @Pattern(
            regexp = "\\d{4}-\\d{3}",
            message = "Postal code must follow format XXXX-XXX"
    )
    @Column(nullable = false)
    private String postalCode;

    @NotBlank
    @Column(nullable = false)
    private String city;

    @NotBlank
    @Column(nullable = false)
    private String country;

    @NotBlank
    @Column(nullable = false)
    private String street;

    protected Address() {}

    public Address(String postalCode, String city, String country, String street) {
        if (!isValid(postalCode, city, country, street)) {
            throw new BusinessException("Invalid address data");
        }
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.street = street;
    }

    private boolean isValid(String postalCode, String city, String country, String street) {
        return isValidPostalCode(postalCode)
                && isNotBlank(city)
                && isNotBlank(country)
                && isNotBlank(street);
    }

    private boolean isValidPostalCode(String postalCode) {
        return postalCode != null && postalCode.matches("\\d{4}-\\d{3}");
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
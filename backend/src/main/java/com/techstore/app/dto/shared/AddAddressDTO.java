package com.techstore.app.dto.shared;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddAddressDTO(
        @NotBlank(message = "Postal code is required") @Pattern(regexp = "\\d{4}-\\d{3}", message = "Postal code must follow format XXXX-XXX") String postalCode,
        @NotBlank String city, @NotBlank String country, @NotBlank String street) {

}

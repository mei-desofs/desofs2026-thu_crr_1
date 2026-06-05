package com.techstore.app.dto.order;

import com.techstore.app.dto.shared.AddAddressDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;

public record CreateOrderRequestDTO(
        @NotBlank(message = "Cart ID is required")
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "Invalid Cart ID format"
        )
        String cartID,

        @NotNull(message = "Address is required")
        @Valid
        AddAddressDTO address,

        @NotBlank(message = "Customer ID is required")
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "Invalid Customer ID format"
        )
        String customerID
) {
}

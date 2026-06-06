package com.techstore.app.dto.order;

import com.techstore.app.dto.shared.AddAddressDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;

public record CreateOrderRequestDTO(
        @NotNull(message = "Address is required")
        @Valid
        AddAddressDTO address
) {
}

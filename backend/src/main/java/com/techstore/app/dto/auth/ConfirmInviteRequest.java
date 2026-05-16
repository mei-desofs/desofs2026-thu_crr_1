package com.techstore.app.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ConfirmInviteRequest(
        @NotBlank(message = "Token hash is required")
        String tokenHash,

        @NotBlank(message = "Type is required")
        String type) {
}

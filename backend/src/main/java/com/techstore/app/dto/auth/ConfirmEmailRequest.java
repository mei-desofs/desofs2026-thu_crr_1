package com.techstore.app.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailRequest(
        @NotBlank String accessToken
) {
}

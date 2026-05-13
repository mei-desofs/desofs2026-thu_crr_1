package com.techstore.app.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record MfaVerifyRequest(
        @NotBlank String factorId,
        @NotBlank String code
) {
}

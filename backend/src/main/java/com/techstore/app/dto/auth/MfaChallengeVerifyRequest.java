package com.techstore.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaChallengeVerifyRequest(
        @NotBlank String factorId,
        @NotBlank String challengeId,
        @NotBlank @Pattern(regexp = "\\d{6}") String code
) {}

package com.techstore.app.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MfaVerifyRequest(
        @JsonProperty("factorId") String factorId,
        @NotBlank String challengeId,
        // V6.5.4: 6-digit code, no truncation (V6.2.8 equivalent for OTP)
        @NotBlank @Pattern(regexp = "\\d{6}") String code
) {}

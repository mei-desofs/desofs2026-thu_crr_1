package com.techstore.app.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record MfaChallengeRequest(@NotBlank String factorId) {}

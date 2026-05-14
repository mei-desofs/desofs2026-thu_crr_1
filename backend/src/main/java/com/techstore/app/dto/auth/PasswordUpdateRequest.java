package com.techstore.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateRequest(
    @NotBlank String userId,
    @NotBlank @Size(min = 12) String newPassword
) {}
package com.techstore.app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for user registration request (only MANAGER and CARRIER roles are allowed).
 * @param email User's email address
 * @param role User's role (must be either MANAGER or CARRIER)
 */
public record InviteSignupRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "MANAGER|CARRIER", message = "Role must be MANAGER or CARRIER")
        String role) {
}

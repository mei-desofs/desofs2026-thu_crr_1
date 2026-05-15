package com.techstore.app.controller;

import com.techstore.app.dto.auth.*;
import com.techstore.app.service.interfaces.AuthService;
import com.techstore.app.service.interfaces.UserService;
import com.techstore.app.config.ratelimit.annotation.RateLimit;
import com.techstore.app.domain.user.User;
import com.techstore.app.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication-related endpoints.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AuthService authService;
    private final UserService userService;
    
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @RateLimit("invite")
    @PostMapping("/invite")
    public ResponseEntity<Void> invite(@RequestBody @Valid InviteSignupRequest request) {
        authService.inviteUser(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm")
public ResponseEntity<?> confirmEmail(@RequestBody ConfirmEmailRequest request) {
    try {
        Map<String, Object> claims = decodeJwtClaims(request.accessToken());
        String supabaseUserId = (String) claims.get("sub");
        String email = (String) claims.get("email");

        if (supabaseUserId == null || supabaseUserId.isBlank() || email == null || email.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        String role = "customer";
        Object userMetadataObject = claims.get("user_metadata");
        if (userMetadataObject instanceof Map<?, ?> userMetadata) {
            Object metadataRole = userMetadata.get("role");
            if (metadataRole instanceof String roleValue && !roleValue.isBlank()) {
                role = roleValue;
            }
        }

        // Verifica se já existe na DB local
        java.util.Optional<User> existing = userService.getUserBySupabaseId(supabaseUserId);
        if (existing.isPresent()) {
            userService.confirmUserEmail(supabaseUserId);
            return ResponseEntity.ok(Map.of("message", "Already confirmed"));
        }

        // Cria agora na DB local
        userService.registerUser(
            supabaseUserId,
            email,
            role
        );
        userService.confirmUserEmail(supabaseUserId);

        return ResponseEntity.ok(Map.of("message", "Email confirmed and user created"));

    } catch (Exception ex) {
        return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
    }
}

    private Map<String, Object> decodeJwtClaims(String accessToken) throws Exception {
        String[] parts = accessToken.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid token format");
        }

        byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
        String payload = new String(decoded, StandardCharsets.UTF_8);
        return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
    }

    @RateLimit("auth")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            RegisterResponse response = authService.register(request, httpRequest);
            return ResponseEntity.status(201).body(response);
        } catch (BusinessException ex) {
            String errorMessage = ex.getMessage();
            if (errorMessage != null && (errorMessage.toLowerCase().contains("already registered")
                    || errorMessage.toLowerCase().contains("already exists"))) {
                RegisterResponse errorResponse = new RegisterResponse(
                        request.email(),
                        null,
                        "Email already registered in Supabase"
                );
                return ResponseEntity.status(409).body(errorResponse);
            }
            throw ex;
        }
    }

    @RateLimit("auth")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequest request, HttpServletRequest httpRequest) {
        authService.logout(request.accessToken(), httpRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam(required = false) String accessToken,
            @RequestParam(required = false) String error, @RequestParam(required = false) String errorDescription) {

        if (error != null) {
            throw new IllegalArgumentException("Error: "
                    + (errorDescription != null && !errorDescription.isBlank()
                    ? errorDescription
                    : "invalid callback"));
        }

        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token is missing in the callback");
        }

        return ResponseEntity.ok("Account created successfully. You can now close this page.");
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {

        LoginResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            @RequestBody @Valid RefreshRequest request,
            HttpServletRequest httpRequest) {

        RefreshResponse response = authService.refreshToken(request.refreshToken(), httpRequest);
        return ResponseEntity.ok(response);
    }
}

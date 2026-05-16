package com.techstore.app.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.app.config.ratelimit.annotation.RateLimit;
import com.techstore.app.dto.auth.PasswordResetRequest;
import com.techstore.app.dto.auth.PasswordUpdateRequest;
import com.techstore.app.dto.auth.*;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.helpers.CookiesHelper;
import com.techstore.app.service.interfaces.AuthService;
import com.techstore.app.service.interfaces.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication-related endpoints.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger();

    private final AuthService authService;
    private final UserService userService;
    
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @RateLimit("invite")
    @PostMapping("/invite")
    public ResponseEntity<Void> invite(@RequestBody @Valid InviteSignupRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        authService.inviteUser(request, clientIp, userAgent);
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

            var existingUser = userService.getUserBySupabaseId(supabaseUserId);
            if (existingUser.isEmpty()) {
                userService.registerUser(supabaseUserId, email, role);
            }
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

    @RateLimit("register")
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

    @RateLimit("logout")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String accessToken = CookiesHelper.getCookieValue(httpRequest, "access_token");

        if (accessToken != null && !accessToken.isBlank()) {
            authService.logout(accessToken, httpRequest);
        }

        // Clear auth cookies regardless
        CookiesHelper.clearAuthCookies(httpResponse);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirm(
            @RequestParam("token_hash") String tokenHash,
            @RequestParam("type") String type) {

        authService.confirmAndSetupAccount(tokenHash, type);
        return ResponseEntity.ok("Email confirmed. Please set your password using POST /auth/set-password with your access token.");
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam(required = false) String accessToken,
            @RequestParam(required = false) String error, @RequestParam(required = false) String errorDescription) {

        if (error != null) {
            logger.warn("Callback error received: {} - {}", error, errorDescription);
            throw new IllegalArgumentException("Invalid callback");
        }

        if (accessToken == null || accessToken.isBlank()) {
            logger.warn("Access token is missing in the callback");
            throw new IllegalArgumentException("Invalid callback");
        }

        return ResponseEntity.ok("Account created successfully. You can now close this page.");
    }

    @RateLimit("login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest,  HttpServletResponse httpResponse) {

        LoginResponse response = authService.login(request, httpRequest);

        // Guarda os tokens em cookies HttpOnly
        CookiesHelper.setAuthCookies(httpResponse, response.accessToken(), response.refreshToken());

        return ResponseEntity.ok().build();
    }
    @RateLimit("refresh-token")
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // Lê o refresh_token do cookie
        String refreshToken = CookiesHelper.getCookieValue(httpRequest, "refresh_token");
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh token not found");
        }

        RefreshResponse response = authService.refreshToken(refreshToken, httpRequest);

        // Atualiza os cookies com os novos tokens
        CookiesHelper.setAuthCookies(httpResponse, response.accessToken(), response.refreshToken());

        return ResponseEntity.ok().build();
    }

    @RateLimit("password-update")
    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestReset(@Valid @RequestBody PasswordResetRequest request, HttpServletRequest httpRequest) {
        authService.requestPasswordReset(request.email(), httpRequest);
        return ResponseEntity.noContent().build();
    }

    @RateLimit("password-update")
    @PostMapping("/set-password")
    public ResponseEntity<Void> setPassword(@RequestHeader("Authorization") String authHeader, @RequestBody @Valid PasswordUpdateRequest request, HttpServletRequest httpRequest) {
        String accessToken = authHeader.replace("Bearer ", "");
        authService.updatePassword(accessToken, request.newPassword(), httpRequest);
        return ResponseEntity.ok().build();
    }
}

package com.techstore.app.controller;

import com.techstore.app.config.ratelimit.annotation.RateLimit;
import com.techstore.app.dto.auth.*;
import com.techstore.app.helpers.CookiesHelper;
import com.techstore.app.service.interfaces.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication-related endpoints.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LogManager.getLogger();

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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

    @PostMapping("/confirm-invite")
    public ResponseEntity<Void> confirmInvite(
            @RequestHeader("x-webhook-secret") String secret,
            @RequestBody Map<String, Object> payload) {
        if (!authService.confirmInvite(secret, payload)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmEmail(@RequestBody ConfirmEmailRequest request) {
        authService.confirmEmailFromRegister(request.accessToken());
        return ResponseEntity.ok(Map.of("message", "Email confirmed and user created"));
    }

    

    @RateLimit("register")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request,
            HttpServletRequest httpRequest) {
        RegisterResponse response = authService.register(request, httpRequest);
        return ResponseEntity.status(201).body(response);
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
        return ResponseEntity
                .ok("Email confirmed. Please set your password using POST /auth/set-password with your access token.");
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
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

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
    public ResponseEntity<Void> requestReset(@Valid @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest) {
        authService.requestPasswordReset(request.email(), httpRequest);
        return ResponseEntity.noContent().build();
    }

    @RateLimit("password-update")
    @PostMapping("/set-password")
    public ResponseEntity<Void> setPassword(@RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid PasswordUpdateRequest request, HttpServletRequest httpRequest) {
        String accessToken = authHeader.replace("Bearer ", "");
        authService.updatePassword(accessToken, request.newPassword(), httpRequest);
        return ResponseEntity.ok().build();
    }
}

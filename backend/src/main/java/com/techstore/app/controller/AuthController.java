package com.techstore.app.controller;

import com.techstore.app.dto.auth.*;
import com.techstore.app.helpers.CookiesHelper;
import com.techstore.app.service.interfaces.AuthService;
import com.techstore.app.config.ratelimit.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication-related endpoints.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @RateLimit("invite")
    @PostMapping("/invite")
    public ResponseEntity<Void> invite(@RequestBody @Valid InviteSignupRequest request) {
        authService.inviteUser(request);
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
    @RateLimit("login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest,  HttpServletResponse httpResponse) {

        LoginResponse response = authService.login(request, httpRequest);

        // Guarda os tokens em cookies HttpOnly
        CookiesHelper.setAuthCookies(httpResponse, response.accessToken(), response.refreshToken());

        return ResponseEntity.ok().build();
    }
    @RateLimit("login")
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
}

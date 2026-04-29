package com.techstore.app.controller;

import com.techstore.app.dto.auth.*;
import com.techstore.app.service.interfaces.AuthService;
import jakarta.servlet.http.HttpServletRequest;
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

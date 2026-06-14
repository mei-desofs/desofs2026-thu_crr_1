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
import org.springframework.security.core.Authentication;
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
        String accessToken = CookiesHelper.getCookieValue(httpRequest, "__Secure-access_token");

        if (accessToken != null && !accessToken.isBlank()) {
            authService.logout(accessToken, httpRequest);
        }

        // Clear both auth cookies so refresh cannot mint a new access token after logout.
        CookiesHelper.clearAuthCookies(httpResponse);

        return ResponseEntity.ok().build();
    }

    @RateLimit("login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {
        LoginResponse response = authService.login(request, httpRequest);

        if (response.mfaRequired()) {
            return ResponseEntity.ok(new LoginResponse(
                    null, null, null, null,
                    true,
                    response.factorId(),
                    response.mfaToken()
            ));
        }

        CookiesHelper.setAuthCookies(httpResponse, response.accessToken(), response.refreshToken());
        return ResponseEntity.ok(new LoginResponse(response.accessToken(), response.refreshToken(), response.tokenType(), response.expiresIn(), false, response.factorId(), response.mfaToken()));
    }

    @RateLimit("refresh-token")
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // Lê o refresh_token do cookie
        String refreshToken = CookiesHelper.getCookieValue(httpRequest, "__Secure-refresh_token");
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

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse(null);
        return ResponseEntity.ok(new MeResponse(role));
    }

    @RateLimit("mfa-enroll")
    @PostMapping("/mfa/enroll")
    public ResponseEntity<MfaEnrollResponse> enrollMfa(HttpServletRequest httpRequest) {
        String accessToken = CookiesHelper.getCookieValue(httpRequest, "__Secure-access_token");
        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        MfaEnrollResponse response = (authService.enrollMfa(accessToken));

        return ResponseEntity.ok(response);
    }

    @RateLimit("mfa-verify")
    @PostMapping("/mfa/verify")
    public ResponseEntity<Void> verifyMfa(
            @RequestBody @Valid MfaVerifyRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        String accessToken = CookiesHelper.getCookieValue(httpRequest, "__Secure-access_token");
        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        authService.verifyMfa(accessToken, request.factorId(),request.challengeId(), request.code());
        return ResponseEntity.ok().build();
    }

    @RateLimit("mfa-challenge")
    @PostMapping("/mfa/challenge")
    public ResponseEntity<MfaChallengeResponse> challengeMfa(
            @RequestBody @Valid MfaChallengeRequest request,
            @RequestHeader("X-MFA-Token") String mfaToken,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(authService.challengeMfa(mfaToken, request.factorId()));
    }

    @RateLimit("mfa-challenge-verify")
    @PostMapping("/mfa/challenge/verify")
    public ResponseEntity<Void> verifyChallengeCode(
            @RequestBody @Valid MfaChallengeVerifyRequest request,
            @RequestHeader("X-MFA-Token") String mfaToken,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        authService.verifyChallengeCode(
                mfaToken, request.factorId(), request.challengeId(), request.code(),
                httpResponse);
        return ResponseEntity.ok().build();
    }
    @RateLimit("mfa-challenge-enroll")
    @PostMapping("/mfa/enroll/challenge")
    public ResponseEntity<MfaChallengeResponse> enrollChallenge(
            @RequestBody @Valid MfaChallengeRequest request,
            HttpServletRequest httpRequest) {
        String accessToken = CookiesHelper.getCookieValue(httpRequest, "__Secure-access_token");
        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authService.challengeForEnroll(accessToken, request.factorId()));
    }

    @RateLimit("mfa-unenroll")
    @DeleteMapping("/mfa/{factorId}")
    public ResponseEntity<Void> unenrollMfa(
            @PathVariable String factorId,
            HttpServletRequest httpRequest) {
        String accessToken = CookiesHelper.getCookieValue(httpRequest, "__Secure-access_token");
        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        authService.unenrollMfa(accessToken, factorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mfa/status")
    public ResponseEntity<MfaStatusResponse> mfaStatus(HttpServletRequest httpRequest) {
        String accessToken = CookiesHelper.getCookieValue(httpRequest, "__Secure-access_token");

        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authService.getMfaStatus(accessToken));
    }

}

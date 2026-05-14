package com.techstore.app.service.interfaces;

import com.techstore.app.dto.auth.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface AuthService {

    /**
     * Invites a user to sign up with the specified email and role.
     * 
     * @param inviteSignupRequest The request containing the email and role for the
     *                            user to be invited.
     * @param clientIp            The client IP address for audit logging.
     * @param userAgent           The user agent string for audit logging.
     */
    void inviteUser(InviteSignupRequest inviteSignupRequest, String clientIp, String userAgent);

    boolean confirmInvite(String secret, Map<String, Object> payload);

    void confirmAndSetupAccount(String tokenHash, String type);

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    RefreshResponse refreshToken(String refreshToken, HttpServletRequest httpRequest);

    void requestPasswordReset(String email, HttpServletRequest httpRequest);

    void updatePassword(String accessToken, String newPassword, HttpServletRequest httpRequest);

}

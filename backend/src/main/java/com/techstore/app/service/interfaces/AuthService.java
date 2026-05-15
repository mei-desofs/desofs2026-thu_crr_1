package com.techstore.app.service.interfaces;

import com.techstore.app.dto.auth.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface AuthService {

    /**
     * Registers a new user with email and password.
     */
    RegisterResponse register(RegisterRequest request, HttpServletRequest httpRequest);

    /**
     * Logs out the user by revoking their access token.
     */
    void logout(String accessToken, HttpServletRequest httpRequest);

    /**
     * Invites a user to sign up with the specified email and role.
     * @param inviteSignupRequest The request containing the email and role for the user to be invited.
     */
    void inviteUser(InviteSignupRequest inviteSignupRequest);

    /**
     * Confirms an invite via webhook from Supabase.
     */
    boolean confirmInvite(String secret, Map<String, Object> payload);

    /**
     * Logs in a user with email and password.
     */
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * Refreshes an access token.
     */
    RefreshResponse refreshToken(String refreshToken, HttpServletRequest httpRequest);

}

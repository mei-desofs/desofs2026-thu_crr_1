package com.techstore.app.service.interfaces;

import com.techstore.app.dto.auth.InviteSignupRequest;
import com.techstore.app.dto.auth.LoginRequest;
import com.techstore.app.dto.auth.LoginResponse;
import com.techstore.app.dto.auth.RefreshResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface AuthService {

    /**
     * Invites a user to sign up with the specified email and role.
     * @param inviteSignupRequest The request containing the email and role for the user to be invited.
     */
    void inviteUser(InviteSignupRequest inviteSignupRequest);

    boolean confirmInvite(String secret, Map<String, Object> payload);

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    RefreshResponse refreshToken(String refreshToken, HttpServletRequest httpRequest); // novo

}

package com.techstore.app.service;

import com.techstore.app.client.SupabaseAuthClient;
import com.techstore.app.domain.shared.EmailAddress;
import com.techstore.app.dto.auth.*;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.logger.AuthAuditLogger;
import com.techstore.app.service.interfaces.AuthService;
import com.techstore.app.service.interfaces.UserService;
import com.techstore.app.util.PasswordUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Value("${supabase.webhook-secret}")
    private String webhookSecret;

    private final UserService userService;

    private final SupabaseAuthClient supabaseAuthClient;

    private final AuthAuditLogger auditLogger;

    public AuthServiceImpl(UserService userService, SupabaseAuthClient supabaseAuthClient,
            AuthAuditLogger auditLogger) {
        this.userService = userService;
        this.supabaseAuthClient = supabaseAuthClient;
        this.auditLogger = auditLogger;
    }

    @Override
    public void inviteUser(InviteSignupRequest inviteSignupRequest, String clientIp, String userAgent) {
        try {
            supabaseAuthClient.inviteUser(inviteSignupRequest.email(), inviteSignupRequest.role());
            auditLogger.logInviteAttempt(inviteSignupRequest.email(), true, clientIp, userAgent);
        } catch (Exception ex) {
            auditLogger.logInviteAttempt(inviteSignupRequest.email(), false, clientIp, userAgent);
            throw ex;
        }
    }

    @Override
    public boolean confirmInvite(String secret, Map<String, Object> payload) {

        if (!webhookSecret.equals(secret)) {
            return false;
        }

        Map<String, Object> record = (Map<String, Object>) payload.get("record");
        Map<String, Object> oldRecord = (Map<String, Object>) payload.get("old_record");

        String emailConfirmedAt = (String) record.get("email_confirmed_at");
        String oldEmailConfirmedAt = (String) oldRecord.get("email_confirmed_at");

        if (emailConfirmedAt == null || oldEmailConfirmedAt != null) {
            return true;
        }

        String supabaseUserId = (String) record.get("id");
        String email = (String) record.get("email");
        Map<String, Object> metadata = (Map<String, Object>) record.get("raw_user_meta_data");
        String role = (String) metadata.get("role");

        if (role != null) {
            userService.registerUser(supabaseUserId, email, role);
        }

        return true;
    }

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            SupabaseLoginResponse supabaseResponse = supabaseAuthClient.login(request.email(), request.password());

            auditLogger.logLoginAttempt(request.email(), true, httpRequest);

            return new LoginResponse(
                    supabaseResponse.accessToken(),
                    supabaseResponse.refreshToken(),
                    supabaseResponse.tokenType(),
                    supabaseResponse.expiresIn());

        } catch (Exception ex) {
            auditLogger.logLoginAttempt(request.email(), false, httpRequest);
            throw ex;
        }
    }

    @Override
    public RefreshResponse refreshToken(String refreshToken, HttpServletRequest httpRequest) {
        try {
            RefreshResponse response = supabaseAuthClient.refreshToken(refreshToken);

            // To Do: extrair o userId do token para o log
            auditLogger.logTokenRefresh("unknown", true, httpRequest);

            return response;

        } catch (Exception ex) {
            auditLogger.logTokenRefresh("unknown", false, httpRequest);
            throw ex;
        }
    }

    @Override
    public void requestPasswordReset(String email, HttpServletRequest httpRequest) {
        try {
            if (!EmailAddress.isValid(email)) {
                throw new BusinessException("Invalid email format");
            }

            supabaseAuthClient.sendPasswordResetEmail(email);
            auditLogger.logPasswordResetRequest(email, true, httpRequest);
        } catch (Exception ex) {
            auditLogger.logPasswordResetRequest(email, false, httpRequest);
            throw ex;
        }
    }

    @Override
    public void updatePassword(String userId, String newPassword, HttpServletRequest httpRequest) {
        try {
            if (!PasswordUtils.isValid(newPassword)) {
                throw new BusinessException("Invalid password format");
            }

            supabaseAuthClient.updatePassword(userId, newPassword);
            auditLogger.logPasswordUpdate(true, httpRequest);
        } catch (Exception ex) {
            auditLogger.logPasswordUpdate(false, httpRequest);
            throw ex;
        }
    }
}

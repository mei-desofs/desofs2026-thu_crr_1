package com.techstore.app.service;

import com.techstore.app.client.SupabaseAuthClient;
import com.techstore.app.dto.auth.*;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.logger.AuthAuditLogger;
import com.techstore.app.service.interfaces.AuthService;
import com.techstore.app.service.interfaces.UserService;
import jakarta.servlet.http.HttpServletRequest;
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

    public AuthServiceImpl(UserService userService, SupabaseAuthClient supabaseAuthClient, AuthAuditLogger auditLogger) {
        this.userService = userService;
        this.supabaseAuthClient = supabaseAuthClient;
        this.auditLogger = auditLogger;
    }

    public void inviteUser(InviteSignupRequest inviteSignupRequest) {
        supabaseAuthClient.inviteUser(inviteSignupRequest.email(), inviteSignupRequest.role());
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
            SupabaseLoginResponse supabaseResponse =
                    supabaseAuthClient.login(request.email(), request.password());

            auditLogger.logLoginAttempt(request.email(), true, httpRequest);

            return new LoginResponse(
                    supabaseResponse.accessToken(),
                    supabaseResponse.refreshToken(),
                    supabaseResponse.tokenType(),
                    supabaseResponse.expiresIn()
            );

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
}

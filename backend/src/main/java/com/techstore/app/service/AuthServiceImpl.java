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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "customer";

    @Value("${supabase.webhook-secret}")
    private String webhookSecret;

    private final UserService userService;

    private final SupabaseAuthClient supabaseAuthClient;

    private final AuthAuditLogger auditLogger;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(UserService userService, SupabaseAuthClient supabaseAuthClient,
            AuthAuditLogger auditLogger) {
        this.userService = userService;
        this.supabaseAuthClient = supabaseAuthClient;
        this.auditLogger = auditLogger;
    }

    @Override
    public RegisterResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            SupabaseLoginResponse supabaseResponse = supabaseAuthClient.signUp(
                    request.email(), request.password(), DEFAULT_ROLE
            );

            String userId = null;
            if (supabaseResponse.user() != null) {
                userId = supabaseResponse.user().id();
            }

            auditLogger.logRegisterAttempt(request.email(), true, httpRequest);

            return new RegisterResponse(
                    request.email(),
                    userId,
                    "Check your email for confirmation link"
            );

        } catch (Exception ex) {
            auditLogger.logRegisterAttempt(request.email(), false, httpRequest);
            throw ex;
        }
    }

    @Override
    public void logout(String accessToken, HttpServletRequest httpRequest) {
        try {
            supabaseAuthClient.revokeToken(accessToken);
            auditLogger.logLogoutAttempt("unknown", true, httpRequest);
        } catch (Exception ex) {
            auditLogger.logLogoutAttempt("unknown", false, httpRequest);
            throw ex;
        }
    }

    @Override
    public void inviteUser(InviteSignupRequest inviteSignupRequest, String clientIp, String userAgent) {

        String role = inviteSignupRequest.role().toUpperCase();
        if (!role.equals("MANAGER") && !role.equals("CARRIER")) {
            throw new BusinessException("Invalid role: only MANAGER and CARRIER can be invited");
        }

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
            auditLogger.logConfirmInvite("unknown", false, "Invalid webhook secret");
            return false;
        }

        if (payload == null) {
            auditLogger.logConfirmInvite("unknown", false, "Null payload");
            return false;
        }

        Map<String, Object> record = (Map<String, Object>) payload.get("record");
        Map<String, Object> oldRecord = (Map<String, Object>) payload.get("old_record");

        if (record == null || oldRecord == null) {
            auditLogger.logConfirmInvite("unknown", false, "Missing record or old_record");
            return false;
        }

        String emailConfirmedAt = (String) record.get("email_confirmed_at");
        String oldEmailConfirmedAt = oldRecord != null ? (String) oldRecord.get("email_confirmed_at") : null;

        if (emailConfirmedAt == null || oldEmailConfirmedAt != null) {
            return true;
        }

        String supabaseUserId = (String) record.get("id");
        String email = (String) record.get("email");
        Map<String, Object> metadata = (Map<String, Object>) record.get("raw_user_meta_data");
        String role = metadata != null ? (String) metadata.get("role") : null;
        if (role == null || role.isBlank()) {
            role = DEFAULT_ROLE;
        }

        if (supabaseUserId == null || email == null) {
            auditLogger.logConfirmInvite("unknown", false, "Missing required fields in payload");
            return false;
        }

        if (!supabaseAuthClient.userExists(supabaseUserId)) {
            auditLogger.logConfirmInvite(email, false, "Supabase user not found - possible payload manipulation");
            return false;
        }

        boolean userAlreadyExists = userService.getUserBySupabaseId(supabaseUserId).isPresent();

        try {
            if (!userAlreadyExists) {
                userService.registerUser(supabaseUserId, email, role);
            }
            userService.confirmUserEmail(supabaseUserId);
            auditLogger.logConfirmInvite(email, true, null);
            return true;

        } catch (BusinessException ex) {
            auditLogger.logConfirmInvite(email, false, ex.getMessage());
            return false;

        } catch (Exception ex) {
            logger.error("Unexpected error registering user {}, rolling back: {}", email, ex.getMessage());
            if (!userAlreadyExists) {
                try {
                    supabaseAuthClient.deleteUser(supabaseUserId);
                } catch (Exception rollbackEx) {
                    logger.error("Failed to rollback Supabase user {} — manual cleanup required", supabaseUserId);
                }
            }
            auditLogger.logConfirmInvite(email, false, "Internal registration failed");
            return false;
        }
    }

    @Override
    public void confirmAndSetupAccount(String tokenHash, String type) {
        supabaseAuthClient.verifyToken(tokenHash, type);
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
    public void updatePassword(String accessToken, String newPassword, HttpServletRequest httpRequest) {
        try {
            if (!PasswordUtils.isValid(newPassword)) {
                throw new BusinessException("Invalid password format");
            }

            supabaseAuthClient.updatePassword(accessToken, newPassword);
            auditLogger.logPasswordUpdate(true, httpRequest);
        } catch (Exception ex) {
            auditLogger.logPasswordUpdate(false, httpRequest);
            throw ex;
        }
    }
}

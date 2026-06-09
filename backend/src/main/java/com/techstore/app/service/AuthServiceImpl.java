package com.techstore.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.app.client.SupabaseAuthClient;
import com.techstore.app.domain.shared.EmailAddress;
import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.Role;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.dto.auth.*;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.helpers.CookiesHelper;
import com.techstore.app.logger.AuthAuditLogger;
import com.techstore.app.repository.UserRepository;
import com.techstore.app.service.interfaces.AuthService;
import com.techstore.app.service.interfaces.NotificationService;
import com.techstore.app.service.interfaces.UserService;
import com.techstore.app.util.PasswordUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "customer";

    @Value("${supabase.webhook-secret}")
    private String webhookSecret;
    @Value("${supabase.jwt-secret}")
    private String jwtSecret;

    private final UserService userService;
    private final UserRepository userRepository;

    private final SupabaseAuthClient supabaseClient;

    private final AuthAuditLogger auditLogger;

    private ObjectMapper objectMapper;

    private final PasswordUtils passwordUtils;

    private final NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(UserService userService, UserRepository userRepository,
                           AuthAuditLogger auditLogger, ObjectMapper objectMapper,
                           SupabaseAuthClient supabaseClient, PasswordUtils passwordUtils,
                           NotificationService notificationService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.auditLogger = auditLogger;
        this.objectMapper = objectMapper;
        this.supabaseClient = supabaseClient;
        this.passwordUtils = passwordUtils;
        this.notificationService = notificationService;
    }

    @Override
    public RegisterResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            Email email = new Email(request.email());
            if (userRepository.existsByEmail(email)) {
                throw new BusinessException("Email already registered");
            }
            passwordUtils.validate(request.password(), request.email());

            SupabaseLoginResponse supabaseResponse = supabaseClient.signUp(
                    request.email(), request.password(), DEFAULT_ROLE);
            String userId = null;
            if (supabaseResponse.user() != null) {
                userId = supabaseResponse.user().id();
            }
            auditLogger.logRegisterAttempt(request.email(), true, httpRequest);
            return new RegisterResponse(
                    request.email(),
                    userId,
                    "Check your email for confirmation link");
        } catch (BusinessException ex) {
            auditLogger.logRegisterAttempt(request.email(), false, httpRequest);
            throw ex;
        } catch (Exception ex) {
            logger.error("Register failed for {}: {}", request.email(), ex.getMessage(), ex);
            auditLogger.logRegisterAttempt(request.email(), false, httpRequest);
            throw ex;
        }
    }

    public void confirmEmailFromRegister(String accessToken) {
        try {
            Map<String, Object> claims = decodeJwtClaims(accessToken);

            String supabaseUserId = (String) claims.get("sub");
            String email = (String) claims.get("email");

            if (supabaseUserId == null || supabaseUserId.isBlank() || email == null || email.isBlank()) {
                throw new BusinessException("Invalid token");
            }

            String role = Role.CUSTOMER.toString();

            if (userService.getUserBySupabaseId(supabaseUserId).isEmpty()) {
                userService.registerUser(supabaseUserId, email, role);
            }

            userService.confirmUserEmail(supabaseUserId);

            try {
                notificationService.sendEmail(email, "TechStore - Registration Completed",
                        createRegistrationCompletedEmailBody(email));
            } catch (Exception exception) {
                logger.warn("Failed to send registration email to {}", email, exception);
            }

        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Failed to confirm email: " + ex.getMessage());
        }

    }

    private Map<String, Object> decodeJwtClaims(String accessToken) throws Exception {
        String[] parts = accessToken.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid token format");
        }

        byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
        String payload = new String(decoded, StandardCharsets.UTF_8);
        return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {
        });
    }

    @Override
    public void logout(String accessToken, HttpServletRequest httpRequest) {
        try {
            supabaseClient.revokeToken(accessToken);
            auditLogger.logLogoutAttempt("unknown", true, httpRequest);
        } catch (Exception ex) {
            logger.warn("Logout failed on Supabase while revoking token. Reason: {}", ex.getMessage());
            auditLogger.logLogoutAttempt("unknown", false, httpRequest);
        }
    }

    @Override
    public void inviteUser(InviteSignupRequest inviteSignupRequest, String clientIp, String userAgent) {

        String role = inviteSignupRequest.role().toUpperCase();
        if (!role.equals("MANAGER") && !role.equals("CARRIER")) {
            throw new BusinessException("Invalid role: only MANAGER and CARRIER can be invited");
        }

        try {
            supabaseClient.inviteUser(inviteSignupRequest.email(), inviteSignupRequest.role());
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
        String invitedAt = (String) record.get("invited_at");

        if (emailConfirmedAt == null || oldEmailConfirmedAt != null || invitedAt == null) {
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

        if (!supabaseClient.userExists(supabaseUserId)) {
            auditLogger.logConfirmInvite(email, false, "Supabase user not found - possible payload manipulation");
            return false;
        }

        boolean userAlreadyExists = userService.getUserBySupabaseId(supabaseUserId).isPresent();

        try {
            if (!userAlreadyExists) {
                userService.registerUser(supabaseUserId, email, role);
            }
            userService.confirmUserEmail(supabaseUserId);

            try {
                notificationService.sendEmail(email, "TechStore - Registration Completed",
                        createRegistrationCompletedEmailBody(email));
            } catch (Exception exception) {
                logger.warn("Failed to send registration email to {}", email, exception);
            }

            auditLogger.logConfirmInvite(email, true, null);
            return true;

        } catch (BusinessException ex) {
            auditLogger.logConfirmInvite(email, false, ex.getMessage());
            return false;

        } catch (Exception ex) {
            logger.error("Unexpected error registering user {}, rolling back: {}", email, ex.getMessage());
            if (!userAlreadyExists) {
                try {
                    supabaseClient.deleteUser(supabaseUserId);
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
        supabaseClient.verifyToken(tokenHash, type);
    }

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            SupabaseLoginResponse supabaseResponse = supabaseClient.login(request.email(), request.password());

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

        String userId = "unknown";

        try {

            RefreshResponse response = supabaseClient.refreshToken(refreshToken);

            userId = extractUserId(response.accessToken());

            auditLogger.logTokenRefresh(userId, true, httpRequest);

            return response;

        } catch (Exception ex) {

            auditLogger.logTokenRefresh(userId, false, httpRequest);
            throw ex;
        }
    }

    private String extractUserId(String token) {
        try {
            String[] chunks = token.split("\\.");

            if (chunks.length != 3) {
                return "unknown";
            }

            String payload = new String(
                    java.util.Base64.getUrlDecoder().decode(chunks[1]),
                    StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> claims = mapper.readValue(payload, Map.class);

            return (String) claims.get("sub");

        } catch (Exception e) {
            return "unknown";
        }
    }

    @Override
    public void requestPasswordReset(String email, HttpServletRequest httpRequest) {
        try {
            if (!EmailAddress.isValid(email)) {
                throw new BusinessException("Invalid email format");
            }

            supabaseClient.sendPasswordResetEmail(email);
            auditLogger.logPasswordResetRequest(email, true, httpRequest);
        } catch (Exception ex) {
            auditLogger.logPasswordResetRequest(email, false, httpRequest);
            throw ex;
        }
    }

    @Override
    public void updatePassword(String accessToken, String newPassword, HttpServletRequest httpRequest) {
        try {
            String supabaseUserId = CookiesHelper.getCurrentUserId();

            String email = userRepository
                    .findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId))
                    .map(user -> user.getEmail().getEmail())
                    .orElse(null);

            passwordUtils.validate(newPassword, email);

            supabaseClient.updatePassword(accessToken, newPassword);

            if (email != null) {
                try {
                    notificationService.sendEmail(email, "TechStore - Password Updated Successfully",
                            createPasswordUpdatedEmailBody());
                } catch (Exception exception) {
                    logger.warn("Failed to send password update email to {}", email, exception);
                }
            }

            auditLogger.logPasswordUpdate(true, httpRequest);

        } catch (Exception ex) {
            auditLogger.logPasswordUpdate(false, httpRequest);
            throw ex;
        }
    }

    private String createPasswordUpdatedEmailBody() {
        return """
            <h3>Password Updated Successfully 🔐</h3>

            <p>Hello,</p>

            <p>Your TechStore account password has been updated successfully.</p>

            <p>If you made this change, no further action is required.</p>

            <p>
                If you did not update your password, please reset your password immediately
                and contact TechStore support.
            </p>

            <br/>

            <p>Thank you,<br/>TechStore Security Team</p>
            """;
    }

    private String createRegistrationCompletedEmailBody(String email) {
        String displayName = email != null && email.contains("@")
                ? email.split("@")[0]
                : "there";

        return """
            <h3>Welcome to TechStore 🎉</h3>

            <p>Hi %s,</p>

            <p>Your TechStore account registration has been completed successfully.</p>

            <p>
                If you did not create this account, please contact TechStore support immediately.
            </p>

            <br/>

            <p>Thank you,<br/>TechStore Team</p>
            """.formatted(displayName);
    }
}

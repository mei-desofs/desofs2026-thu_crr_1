package com.techstore.app.logger;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuthAuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    public void logLoginAttempt(String email, boolean success, HttpServletRequest request) {

        // Sanitize input to avoid dependency injection
        String userAgent = request.getHeader("User-Agent");
        userAgent = truncate(sanitize(userAgent), 200);

        auditLog.info("event=LOGIN_ATTEMPT | success={} | email={} | ip={} | userAgent={} | timestamp={}",
                success,
                maskEmail(email),
                request.getRemoteAddr(),
                userAgent,
                Instant.now()
        );
    }

    public void logRegisterAttempt(String email, boolean success, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        userAgent = truncate(sanitize(userAgent), 200);

        auditLog.info("event=REGISTER_ATTEMPT | success={} | email={} | ip={} | userAgent={} | timestamp={}",
                success,
                maskEmail(email),
                request.getRemoteAddr(),
                userAgent,
                Instant.now()
        );
    }

    public void logLogoutAttempt(String userId, boolean success, HttpServletRequest request) {
        auditLog.info("event=LOGOUT_ATTEMPT | success={} | userId={} | ip={} | timestamp={}",
                success,
                userId,
                request.getRemoteAddr(),
                Instant.now()
        );
    }
    public void logTokenRefresh(String userId, boolean success, HttpServletRequest request) {
        auditLog.info("event=TOKEN_REFRESH | success={} | userId={} | ip={} | timestamp={}",
                success,
                userId,
                request.getRemoteAddr(),
                Instant.now()
        );
    }

    public void logInviteAttempt(String email, boolean success, String clientIp, String userAgent) {
        userAgent = truncate(sanitize(userAgent), 200);

        auditLog.info("event=INVITE_ATTEMPT | success={} | email={} | ip={} | userAgent={} | timestamp={}",
                success, maskEmail(email), clientIp, userAgent, Instant.now());
    }

    public void logConfirmInvite(String email, boolean success, String reason) {
        auditLog.info("event=CONFIRM_INVITE | success={} | email={} | reason={} | timestamp={}",
                success,
                maskEmail(email),
                reason != null ? reason : "User confirmed email",
                Instant.now());
    }

    public void logPasswordResetRequest(String email, boolean success, HttpServletRequest request) {
        auditLog.info("event=PASSWORD_RESET_REQUEST | success={} | email={} | ip={} | timestamp={}",
                success,
                maskEmail(email),
                request.getRemoteAddr(),
                Instant.now()
        );
    }

    public void logPasswordUpdate(boolean success, HttpServletRequest request) {
        auditLog.info("event=PASSWORD_UPDATE | success={} | ip={} | timestamp={}",
                success,
                request.getRemoteAddr(),
                Instant.now()
        );
    }

    // To not expose sensitive data
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String local = parts[0];
        if (local.length() <= 2) return "**@" + parts[1];
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + parts[1];
    }
    public void logMfaEnrollAttempt(String userId, boolean success) {
        auditLog.info("event=MFA_ENROLL_{} | userId={} | timestamp={}",
                success ? "SUCCESS" : "FAILURE",
                sanitize(userId), System.currentTimeMillis());
    }

    public void logMfaVerifyAttempt(String userId, boolean success) {
        auditLog.info("event=MFA_VERIFY_{} | userId={} | timestamp={}",
                success ? "SUCCESS" : "FAILURE",
                sanitize(userId), System.currentTimeMillis());
    }

    public void logMfaChallengeAttempt(String userId, boolean success) {
        auditLog.info("event=MFA_CHALLENGE_{} | userId={} | timestamp={}",
                success ? "SUCCESS" : "FAILURE",
                sanitize(userId), System.currentTimeMillis());
    }

    public void logMfaChallengeVerify(String userId, boolean success) {
        auditLog.info("event=MFA_CHALLENGE_VERIFY_{} | userId={} | timestamp={}",
                success ? "SUCCESS" : "FAILURE",
                sanitize(userId), System.currentTimeMillis());
    }

    public void logMfaUnenroll(String userId, boolean success) {
        auditLog.info("event=MFA_UNENROLL_{} | userId={} | timestamp={}",
                success ? "SUCCESS" : "FAILURE",
                sanitize(userId), System.currentTimeMillis());
    }

    private String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll("[\\r\\n\\t]", "_").replaceAll("\\p{Cntrl}", "");
    }

    private String truncate(String input, int maxLength) {
        if (input == null) return null;
        return input.length() <= maxLength ? input : input.substring(0, maxLength);
    }
}

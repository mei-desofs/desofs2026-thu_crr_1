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
    public void logTokenRefresh(String userId, boolean success, HttpServletRequest request) {
        auditLog.info("event=TOKEN_REFRESH | success={} | userId={} | ip={} | timestamp={}",
                success,
                userId,
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
    private String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll("[\\r\\n\\t]", "_")
                .replaceAll("\\p{Cntrl}", "");
    }
    private String truncate(String input, int maxLength) {
        if (input == null) return null;
        return input.length() <= maxLength ? input : input.substring(0, maxLength);
    }
}

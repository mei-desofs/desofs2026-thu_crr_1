package com.techstore.app.service;

import com.techstore.app.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MfaTokenService {

    private static final Duration TTL = Duration.ofMinutes(5);

    private final Map<String, MfaSession> store = new ConcurrentHashMap<>();

    public record MfaSession(String supabaseAccessToken, String supabaseRefreshToken,
                             String factorId, Instant expiresAt) {}

    public String createMfaToken(String accessToken, String refreshToken, String factorId) {
        String token = UUID.randomUUID().toString();
        store.put(token, new MfaSession(
            accessToken, refreshToken, factorId,
            Instant.now().plus(TTL)
        ));
        return token;
    }

    public MfaSession consumeSession(String mfaToken) {
        MfaSession session = store.remove(mfaToken);
        if (session == null || Instant.now().isAfter(session.expiresAt())) {
            throw new BusinessException("MFA session expired or invalid");
        }
        return session;
    }

    public MfaSession peekSession(String mfaToken) {
        MfaSession session = store.get(mfaToken);
        if (session == null || Instant.now().isAfter(session.expiresAt())) {
            throw new BusinessException("MFA session expired or invalid");
        }
        return session;
    }
}
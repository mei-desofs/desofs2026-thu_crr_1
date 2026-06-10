package com.techstore.app.dto.auth;

public record MfaChallengeResponse(
        String id,        // challengeId
        @com.fasterxml.jackson.annotation.JsonProperty("factor_id") String factorId,
        @com.fasterxml.jackson.annotation.JsonProperty("expires_at") long expiresAt
) {}

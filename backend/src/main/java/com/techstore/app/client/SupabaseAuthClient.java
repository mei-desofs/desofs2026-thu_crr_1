package com.techstore.app.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.app.dto.auth.RefreshResponse;
import com.techstore.app.dto.auth.SupabaseLoginResponse;
import com.techstore.app.dto.auth.SupabaseUserResponse;
import com.techstore.app.exception.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class SupabaseAuthClient {

    private static final String AUTH_ADMIN_USERS = "/auth/v1/admin/users";
    private static final String AUTH_INVITE = "/auth/v1/invite";
    private static final String AUTH_VERIFY = "/auth/v1/verify";
    private static final String AUTH_TOKEN = "/auth/v1/token?grant_type=password";
    private static final String AUTH_REFRESH = "/auth/v1/token?grant_type=refresh_token";

    private final String supabaseUrl;

    private final String redirectUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SupabaseAuthClient(@Value("${supabase.url}") String supabaseUrl,
                              @Value("${supabase.redirect-url}") String redirectUrl,
                              @Qualifier("supabaseRestTemplate") RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.supabaseUrl = supabaseUrl;
        this.redirectUrl = redirectUrl;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void inviteUser(String email, String role) {
        String url = supabaseUrl + AUTH_INVITE;

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("email", email, "data", Map.of("role", role),
                        "redirect_to", redirectUrl));

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }

    public void deleteUser(String userId) {
        String url = supabaseUrl + AUTH_ADMIN_USERS + userId;

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }

    public SupabaseUserResponse verifyInviteToken(String tokenHash, String type) {
        String url = supabaseUrl + AUTH_VERIFY;

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("token_hash", tokenHash, "type", type)
        );

        try {
            ResponseEntity<SupabaseUserResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, SupabaseUserResponse.class);

            if (response.getBody() == null) {
                throw new IllegalStateException("Empty response from Supabase.");
            }

            return response.getBody();

        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }

    private RuntimeException mapException(HttpStatusCodeException ex) {
        String message = extractMessage(ex.getResponseBodyAsString());
        int status = ex.getStatusCode().value();

        if (status >= 400 && status < 500) {
            return new BusinessException(normalizeMessage(message));
        }

        return new IllegalStateException("Supabase internal error.");
    }

    private String extractMessage(String body) {
        if (body == null || body.isBlank()) {
            return "Unknown error.";
        }

        try {
            JsonNode json = objectMapper.readTree(body);

            if (json.hasNonNull("msg")) return json.get("msg").asText();
            if (json.hasNonNull("message")) return json.get("message").asText();
            if (json.hasNonNull("error_description")) return json.get("error_description").asText();
            if (json.hasNonNull("error")) return json.get("error").asText();

            return body;
        } catch (Exception e) {
            return body;
        }
    }

    private String normalizeMessage(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("already registered") || lower.contains("duplicate"))
            return "User with this email already exists.";

        if (lower.contains("password"))
            return "Password does not meet the requirements.";

        if (lower.contains("rate limit") || lower.contains("too many"))
            return "Too many requests. Please try again later.";

        if (lower.contains("invalid email format"))
            return "Invalid email.";

        return message;
    }
    public SupabaseLoginResponse login(String email, String password){
        String url = supabaseUrl + AUTH_TOKEN;

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("email", email, "password", password)
        );

        try {
            ResponseEntity<SupabaseLoginResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, SupabaseLoginResponse.class
            );

            if (response.getBody() == null) {
                throw new IllegalStateException("Empty response from Supabase.");
            }

            return response.getBody();

        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }
    public RefreshResponse refreshToken(String refreshToken) {
        String url = supabaseUrl + AUTH_REFRESH;

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("refresh_token", refreshToken)
        );

        try {
            ResponseEntity<RefreshResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, RefreshResponse.class
            );

            if (response.getBody() == null) {
                throw new IllegalStateException("Empty response from Supabase.");
            }

            return response.getBody();

        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }
}

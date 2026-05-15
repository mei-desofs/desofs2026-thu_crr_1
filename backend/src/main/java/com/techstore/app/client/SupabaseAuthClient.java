package com.techstore.app.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.app.dto.auth.RefreshResponse;
import com.techstore.app.dto.auth.SupabaseLoginResponse;
import com.techstore.app.dto.auth.SupabaseUserResponse;
import com.techstore.app.exception.SecurityException;
import com.techstore.app.util.ErrorCodeConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class SupabaseAuthClient {

    private static final String AUTH_ADMIN_USERS = "/auth/v1/admin/users/";
    private static final String AUTH_INVITE = "/auth/v1/invite";
    private static final String AUTH_VERIFY = "/auth/v1/verify";
    private static final String AUTH_SIGNUP = "/auth/v1/signup";
    private static final String AUTH_TOKEN = "/auth/v1/token?grant_type=password";
    private static final String AUTH_REFRESH = "/auth/v1/token?grant_type=refresh_token";
    private static final String AUTH_REVOKE = "/auth/v1/token";
    private static final String AUTH_USER = "/auth/v1/user";
    private static final String AUTH_RECOVER = "/auth/v1/recover";
    private final String supabaseUrl;

    private final String redirectUrl;
    private final String supabaseAnonKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SupabaseAuthClient(@Value("${supabase.url}") String supabaseUrl,
                              @Value("${supabase.anon-key}") String supabaseAnonKey,
                              @Value("${supabase.redirect-url}") String redirectUrl,
                              @Qualifier("supabaseRestTemplate") RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.supabaseUrl = supabaseUrl;
        this.supabaseAnonKey = supabaseAnonKey;
        this.redirectUrl = redirectUrl;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean userExists(String supabaseUserId) {
        String url = supabaseUrl + AUTH_ADMIN_USERS + "/" + supabaseUserId;
        try {
            restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, Void.class);
            return true;
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 404) return false;
            throw mapException(ex);
        }
    }

    public void inviteUser(String email, String role) {
        String url = supabaseUrl + AUTH_INVITE;

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of(
                        "email", email,
                        "data", Map.of("role", role),
                        "redirect_to", redirectUrl
                )
        );

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }

    public void deleteUser(String userId) {
        String url = supabaseUrl + AUTH_ADMIN_USERS + "/" + userId;

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }

    public SupabaseUserResponse verifyInviteToken(String tokenHash, String type) {
        String url = supabaseUrl + AUTH_VERIFY;

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("token_hash", tokenHash, "type", type));

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
            String code = determineErrorCode(message);
            return new SecurityException("Request failed", code);
        }

        return new IllegalStateException("Supabase internal error.");
    }

    /**
     * Extracts error message from Supabase response body.
     * Tries multiple common error response formats.
     */
    private String extractMessage(String body) {
        if (body == null || body.isBlank()) {
            return "Unknown error.";
        }

        try {
            JsonNode json = objectMapper.readTree(body);

            if (json.hasNonNull("msg"))
                return json.get("msg").asText();
            if (json.hasNonNull("message"))
                return json.get("message").asText();
            if (json.hasNonNull("error_description"))
                return json.get("error_description").asText();
            if (json.hasNonNull("error"))
                return json.get("error").asText();

            return body;
        } catch (Exception e) {
            return body;
        }
    }

    private String determineErrorCode(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("already registered") || lower.contains("duplicate")) {
            return ErrorCodeConstants.AUTH_DUPLICATE_EMAIL;
        }

        if (lower.contains("password")) {
            return ErrorCodeConstants.AUTH_INVALID_PASSWORD;
        }

        if (lower.contains("rate limit") || lower.contains("too many")) {
            return ErrorCodeConstants.RATE_LIMIT;
        }

        if (lower.contains("invalid email format")) {
            return ErrorCodeConstants.AUTH_INVALID_EMAIL;
        }

        if (lower.contains("invalid") || lower.contains("unauthorized")) {
            return ErrorCodeConstants.AUTH_INVALID_CREDENTIALS;
        }

        return ErrorCodeConstants.AUTH_SERVICE_ERROR;
    }

    public SupabaseLoginResponse login(String email, String password) {

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("email", email, "password", password));

        try {
            ResponseEntity<SupabaseLoginResponse> response = restTemplate.exchange(
                    (supabaseUrl + AUTH_TOKEN), HttpMethod.POST, entity, SupabaseLoginResponse.class);

            if (response.getBody() == null) {
                throw new IllegalStateException("Empty response from Supabase.");
            }

            return response.getBody();

        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }

    public SupabaseLoginResponse signUp(String email, String password, String role) {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of(
                        "email", email,
                        "password", password,
                        "data", Map.of("role", role)
                )
        );

        try {
            ResponseEntity<SupabaseLoginResponse> response = restTemplate.exchange(
                    (supabaseUrl + AUTH_SIGNUP), HttpMethod.POST, entity, SupabaseLoginResponse.class
            );

            if (response.getBody() == null) {
                throw new IllegalStateException("Empty response from Supabase.");
            }

            return response.getBody();

        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }

    public void revokeToken(String accessToken) {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("token", accessToken),
                createHeaders()
        );

        try {
            restTemplate.exchange(supabaseUrl + AUTH_REVOKE, HttpMethod.POST, entity, Void.class);
        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public SupabaseUserResponse getUser(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<SupabaseUserResponse> response = restTemplate.exchange(
                    supabaseUrl + AUTH_USER,
                    HttpMethod.GET,
                    entity,
                    SupabaseUserResponse.class
            );
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }

    public RefreshResponse refreshToken(String refreshToken) {

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("refresh_token", refreshToken));

        try {
            ResponseEntity<RefreshResponse> response = restTemplate.exchange(
                    (supabaseUrl + AUTH_REFRESH), HttpMethod.POST, entity, RefreshResponse.class);

            if (response.getBody() == null) {
                throw new IllegalStateException("Empty response from Supabase.");
            }

            return response.getBody();

        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }

    public void verifyToken(String tokenHash, String type) {
        String url = supabaseUrl + AUTH_VERIFY;

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("token_hash", tokenHash, "type", type)
        );

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }

    public void sendPasswordResetEmail(String email) {
        String url = supabaseUrl + AUTH_RECOVER;

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("email", email, "redirect_to", redirectUrl)
        );

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }

    public void updatePassword(String accessToken, String password) {
        String url = supabaseUrl + AUTH_USER;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseAnonKey);
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("password", password),
                headers
        );

        try {
            new RestTemplate().exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (HttpStatusCodeException ex) {
            throw mapException(ex);
        }
    }
}

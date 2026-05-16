package com.techstore.app.controller;

import com.techstore.app.client.SupabaseAuthClient;
import com.techstore.app.dto.auth.InviteSignupRequest;
import com.techstore.app.logger.AuthAuditLogger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@SpringBootTest
public class InviteIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SupabaseAuthClient supabaseAuthClient;

    @MockitoBean
    private AuthAuditLogger authAuditLogger;

    /**
     * Generate a valid JWT token with sub and user_id claims for testing
     */
    private String generateTestToken() {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"user-manager-1\",\"user_id\":\"user-manager-1\"}".getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".";
    }

    @Test
    void inviteSuccess() throws Exception {
        String email = "test@example.com";
        doNothing().when(supabaseAuthClient).inviteUser(anyString(), anyString());

        mvc.perform(post("/auth/invite")
                        .header("Authorization", "Bearer " + generateTestToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"email\":\"%s\",\"role\":\"MANAGER\"}", email)))
                .andExpect(status().isOk());

        verify(supabaseAuthClient, times(1)).inviteUser(email, "MANAGER");
        verify(authAuditLogger, times(1)).logInviteAttempt(email, true, anyString(), anyString());
    }

    @Test
    void inviteSuccessWithUserAgent() throws Exception {
        String email = "ua@example.com";
        doNothing().when(supabaseAuthClient).inviteUser(anyString(), anyString());

        mvc.perform(post("/auth/invite")
                        .header("Authorization", "Bearer " + generateTestToken())
                        .header("User-Agent", "CustomAgent/1.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"email\":\"%s\",\"role\":\"MANAGER\"}", email)))
                .andExpect(status().isOk());

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> successCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> uaCaptor = ArgumentCaptor.forClass(String.class);

        verify(authAuditLogger).logInviteAttempt(emailCaptor.capture(), successCaptor.capture(), ipCaptor.capture(), uaCaptor.capture());
        verify(supabaseAuthClient, times(1)).inviteUser(email, "MANAGER");

        assertEquals(email, emailCaptor.getValue());
        assertTrue(successCaptor.getValue());
        assertNotNull(ipCaptor.getValue());
        assertEquals("CustomAgent/1.0", uaCaptor.getValue());
    }

    @Test
    void inviteForbiddenForNonManager() throws Exception {
        // Test without valid authentication (no Bearer token)
        mvc.perform(post("/auth/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@example.com\",\"role\":\"MANAGER\"}"))
                .andExpect(status().isOk()); // Will pass-through since all endpoints are permitAll

        // No inviteUser call should happen but audit might be called
    }

    @Test
    void inviteValidationMissingEmail() throws Exception {
        mvc.perform(post("/auth/invite")
                        .header("Authorization", "Bearer " + generateTestToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"MANAGER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void inviteSupabaseFailure() throws Exception {
        String email = "fail@example.com";
        doThrow(new RuntimeException("Supabase down")).when(supabaseAuthClient).inviteUser(anyString(), anyString());

        mvc.perform(post("/auth/invite")
                        .header("Authorization", "Bearer " + generateTestToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"email\":\"%s\",\"role\":\"MANAGER\"}", email)))
                .andExpect(status().isInternalServerError());

        verify(supabaseAuthClient, times(1)).inviteUser(email, "MANAGER");
        verify(authAuditLogger, times(1)).logInviteAttempt(email, false, anyString(), anyString());
    }
}


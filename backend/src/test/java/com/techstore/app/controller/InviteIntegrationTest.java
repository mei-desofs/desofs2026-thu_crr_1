package com.techstore.app.controller;

import com.techstore.app.client.SupabaseAuthClient;
import com.techstore.app.config.jwt.JWTAuthFilter;
import com.techstore.app.logger.AuthAuditLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class InviteIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    @MockitoBean
    private SupabaseAuthClient supabaseAuthClient;

    @MockitoBean
    private AuthAuditLogger authAuditLogger;

    private String currentUserId;

    @BeforeEach
    void setUpJwtFilter() throws Exception {
        currentUserId = "test-user-" + java.util.UUID.randomUUID();

        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            currentUserId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    private Cookie accessTokenCookie(String userId) {
        String payload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(("{\"sub\":\"" + userId + "\"}")
                        .getBytes(StandardCharsets.UTF_8));

        return new Cookie("__Secure-access_token", "header." + payload + ".signature");
    }

    @Test
    void inviteManagerSuccess() throws Exception {
        String email = "newmanager@example.com";
        doNothing().when(supabaseAuthClient).inviteUser(anyString(), anyString());

        mvc.perform(post("/auth/invite")
                        .cookie(accessTokenCookie(currentUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"role\":\"MANAGER\"}"))
                .andExpect(status().isOk());

        verify(supabaseAuthClient, times(1)).inviteUser(eq(email), eq("MANAGER"));
        verify(authAuditLogger, times(1))
                .logInviteAttempt(eq(email), eq(true), anyString(), isNull());
    }

    @Test
    void inviteCarrierSuccess() throws Exception {
        String email = "newcarrier@example.com";
        doNothing().when(supabaseAuthClient).inviteUser(anyString(), anyString());

        mvc.perform(post("/auth/invite")
                        .cookie(accessTokenCookie(currentUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"role\":\"CARRIER\"}"))
                .andExpect(status().isOk());

        verify(supabaseAuthClient, times(1)).inviteUser(eq(email), eq("CARRIER"));
        verify(authAuditLogger, times(1))
                .logInviteAttempt(eq(email), eq(true), anyString(), isNull());
    }

    @Test
    void inviteWithUserAgent() throws Exception {
        String email = "ua@example.com";
        doNothing().when(supabaseAuthClient).inviteUser(anyString(), anyString());

        mvc.perform(post("/auth/invite")
                        .cookie(accessTokenCookie(currentUserId))
                        .header("User-Agent", "Mozilla/5.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"role\":\"MANAGER\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> successCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> uaCaptor = ArgumentCaptor.forClass(String.class);

        verify(authAuditLogger).logInviteAttempt(
                emailCaptor.capture(),
                successCaptor.capture(),
                ipCaptor.capture(),
                uaCaptor.capture()
        );

        assertEquals(email, emailCaptor.getValue());
        assertTrue(successCaptor.getValue());
        assertNotNull(ipCaptor.getValue());
        assertEquals("Mozilla/5.0", uaCaptor.getValue());
    }

    @Test
    void inviteInvalidRoleFails() throws Exception {
        String email = "user@example.com";

        mvc.perform(post("/auth/invite")
                        .cookie(accessTokenCookie(currentUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());

        verify(supabaseAuthClient, never()).inviteUser(anyString(), anyString());
    }

    @Test
    void inviteMissingEmailFails() throws Exception {
        mvc.perform(post("/auth/invite")
                        .cookie(accessTokenCookie(currentUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"MANAGER\"}"))
                .andExpect(status().isBadRequest());

        verify(supabaseAuthClient, never()).inviteUser(anyString(), anyString());
    }

    @Test
    void inviteMissingRoleFails() throws Exception {
        String email = "user@example.com";

        mvc.perform(post("/auth/invite")
                        .cookie(accessTokenCookie(currentUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isBadRequest());

        verify(supabaseAuthClient, never()).inviteUser(anyString(), anyString());
    }

    @Test
    void supabaseErrorReturns500() throws Exception {
        String email = "fail@example.com";

        doThrow(new RuntimeException("Supabase connection failed"))
                .when(supabaseAuthClient).inviteUser(anyString(), anyString());

        mvc.perform(post("/auth/invite")
                        .cookie(accessTokenCookie(currentUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"role\":\"MANAGER\"}"))
                .andExpect(status().isInternalServerError());

        verify(supabaseAuthClient, times(1)).inviteUser(eq(email), eq("MANAGER"));
        verify(authAuditLogger, times(1))
                .logInviteAttempt(eq(email), eq(false), anyString(), isNull());
    }

    @Test
    void noTokenReturnsError() throws Exception {
        mvc.perform(post("/auth/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"role\":\"MANAGER\"}"))
                .andExpect(status().is4xxClientError());

        verify(supabaseAuthClient, never()).inviteUser(anyString(), anyString());
    }
}
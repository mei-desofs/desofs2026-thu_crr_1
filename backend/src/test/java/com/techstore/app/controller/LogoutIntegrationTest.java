package com.techstore.app.controller;

import com.techstore.app.config.jwt.JWTAuthFilter;
import com.techstore.app.service.interfaces.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LogoutIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    @MockitoBean
    private AuthService authService;

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
                            List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
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

        return new Cookie("access_token", "header." + payload + ".signature");
    }

    @Test
    void logoutClearsBothAuthCookies() throws Exception {
        Cookie accessToken = accessTokenCookie(currentUserId);
        Cookie refreshToken = new Cookie("refresh_token", "refresh-token-value");

        mvc.perform(post("/auth/logout")
                        .cookie(accessToken, refreshToken))
                .andExpect(status().isOk())
                .andExpect(cookie().value("access_token", ""))
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(cookie().value("refresh_token", ""))
                .andExpect(cookie().maxAge("refresh_token", 0));

        verify(authService).logout(eq(accessToken.getValue()), any(HttpServletRequest.class));
    }
}
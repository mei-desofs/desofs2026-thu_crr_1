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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUpJwtFilter() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            "user-1",
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

        return new Cookie("access_token", "header." + payload + ".signature");
    }

    @Test
    void inviteRateLimit() throws Exception {
        for (int i = 1; i <= 5; i++) {
            mvc.perform(post("/auth/invite")
                            .cookie(accessTokenCookie("user-1"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format(
                                    "{\"email\":\"test%1$d@example.com\",\"role\":\"MANAGER\"}", i
                            )))
                    .andExpect(status().isOk());
        }

        mvc.perform(post("/auth/invite")
                        .cookie(accessTokenCookie("user-1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"blocked@example.com\",\"role\":\"MANAGER\"}"))
                .andExpect(status().isTooManyRequests());
    }
}
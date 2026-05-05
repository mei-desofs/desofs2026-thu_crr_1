package com.techstore.app.controller;

import com.techstore.app.service.interfaces.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@AutoConfigureMockMvc
public class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AuthService authService;

    private String token() {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"user-1\"}".getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".";
    }

    @Test
    void inviteRateLimit() throws Exception {
        for (int i = 1; i <= 5; i++) {
            mvc.perform(post("/auth/invite")
                            .header("Authorization", "Bearer " + token())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("{\"email\":\"test%1$d@example.com\",\"role\":\"MANAGER\"}", i)))
                    .andExpect(status().isOk());
        }

        mvc.perform(post("/auth/invite")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"blocked@example.com\",\"role\":\"MANAGER\"}"))
                .andExpect(status().isTooManyRequests());
    }
}

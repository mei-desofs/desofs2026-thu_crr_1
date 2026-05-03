package com.techstore.app.controller;

import com.techstore.app.service.interfaces.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@AutoConfigureMockMvc
public class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AuthService authService;

    private final String token = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ1c2VyLTEifQ.";

    @Test
    void inviteRateLimit() throws Exception {
        for (int i = 1; i <= 5; i++) {
            mvc.perform(post("/auth/invite")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("{\"email\":\"test%1$d@example.com\",\"role\":\"MANAGER\"}", i)))
                    .andExpect(status().isOk());
        }

        mvc.perform(post("/auth/invite")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"blocked@example.com\",\"role\":\"MANAGER\"}"))
                .andExpect(status().isTooManyRequests());
    }
}

package com.techstore.app.controller;

import com.techstore.app.config.jwt.JWTAuthFilter;
import com.techstore.app.domain.cart.Cart;
import com.techstore.app.domain.customer.Customer;
import com.techstore.app.domain.product.Product;
import com.techstore.app.domain.carrier.Carrier;
import com.techstore.app.service.interfaces.AuthService;
import com.techstore.app.service.interfaces.NotificationService;
import com.techstore.app.testutil.TestDataFactory;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUpJwtFilter() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);

            String userId = extractUserIdFromAccessTokenCookie(request);
            String role = extractRoleFromAccessTokenCookie(request);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            SecurityContextHolder.clearContext();
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    private Cookie accessTokenCookie(String userId, String role) {
        String payload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(("{\"sub\":\"" + userId + "\",\"role\":\"" + role + "\"}")
                        .getBytes(StandardCharsets.UTF_8));

        return new Cookie("access_token", "header." + payload + ".signature");
    }

    private String extractUserIdFromAccessTokenCookie(HttpServletRequest request) {
        String payloadJson = extractPayloadJson(request);

        if (payloadJson == null) {
            return "test-user";
        }

        String marker = "\"sub\":\"";
        int start = payloadJson.indexOf(marker);

        if (start == -1) {
            return "test-user";
        }

        start += marker.length();
        int end = payloadJson.indexOf("\"", start);

        if (end == -1) {
            return "test-user";
        }

        return payloadJson.substring(start, end);
    }

    private String extractRoleFromAccessTokenCookie(HttpServletRequest request) {
        String payloadJson = extractPayloadJson(request);

        if (payloadJson == null) {
            return "CUSTOMER";
        }

        String marker = "\"role\":\"";
        int start = payloadJson.indexOf(marker);

        if (start == -1) {
            return "CUSTOMER";
        }

        start += marker.length();
        int end = payloadJson.indexOf("\"", start);

        if (end == -1) {
            return "CUSTOMER";
        }

        return payloadJson.substring(start, end);
    }

    private String extractPayloadJson(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if ("access_token".equals(cookie.getName())) {
                String[] parts = cookie.getValue().split("\\.");

                if (parts.length < 2) {
                    return null;
                }

                return new String(
                        Base64.getUrlDecoder().decode(parts[1]),
                        StandardCharsets.UTF_8
                );
            }
        }

        return null;
    }

    @Test
    void inviteRateLimit() throws Exception {
        Cookie managerCookie = accessTokenCookie("manager-user", "MANAGER");

        for (int i = 1; i <= 5; i++) {
            mvc.perform(post("/auth/invite")
                            .with(csrf())
                            .cookie(managerCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format(
                                    "{\"email\":\"test%1$d@example.com\",\"role\":\"MANAGER\"}", i
                            )))
                    .andExpect(status().isOk());
        }

        mvc.perform(post("/auth/invite")
                        .with(csrf())
                        .cookie(managerCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"blocked@example.com\",\"role\":\"MANAGER\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void createOrderRateLimit() throws Exception {
        Cookie customerCookie = accessTokenCookie("customer-user", "CUSTOMER");

        Product product = testDataFactory.product();

        for (int i = 1; i <= 20; i++) {
            Customer customer = testDataFactory.customer();
            Cart cart = testDataFactory.cartWithItem(product, customer);

            mvc.perform(post("/orders")
                            .with(csrf())
                            .cookie(customerCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(testDataFactory.createOrderJson(
                                    cart.getId().getId().toString(),
                                    customer.getId().getId().toString()
                            )))
                    .andExpect(status().isOk());
        }

        Customer blockedCustomer = testDataFactory.customer();
        Cart blockedCart = testDataFactory.cartWithItem(product, blockedCustomer);

        mvc.perform(post("/orders")
                        .with(csrf())
                        .cookie(customerCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testDataFactory.createOrderJson(
                                blockedCart.getId().getId().toString(),
                                blockedCustomer.getId().getId().toString()
                        )))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void CustomerListOrderRateLimit() throws Exception {

        Cookie customerCookie = accessTokenCookie("customer-user", "CUSTOMER");

        Customer customer = testDataFactory.customer();
        String customerId = customer.getId().getId().toString();

        for (int i = 1; i <= 30; i++) {
            mvc.perform(get("/orders")
                            .param("customerId", customerId)
                            .cookie(customerCookie))
                    .andExpect(status().isOk());
        }
        mvc.perform(get("/orders")
                        .param("customerId", customerId)
                        .cookie(customerCookie))
                .andExpect(status().isTooManyRequests());
    }
    @Test
    void CarrierListOrderRateLimit() throws Exception {

        Cookie carrierCookie = accessTokenCookie("carrier-user", "CARRIER");

        Carrier carrier = testDataFactory.carrier();
        String carrierId = carrier.getId().getId().toString();

        for (int i = 1; i <= 30; i++) {
            mvc.perform(get("/orders/carrier")
                            .param("carrierId", carrierId)
                            .cookie(carrierCookie))
                    .andExpect(status().isOk());
        }
        mvc.perform(get("/orders/carrier")
                        .param("carrierId", carrierId)
                        .cookie(carrierCookie))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void CarrierPickupRateLimit() throws Exception {
        String supabaseUserId = UUID.randomUUID().toString();
        Cookie carrierCookie  = accessTokenCookie(supabaseUserId, "CARRIER");

        for (int i = 0; i < 10; i++) {
            mvc.perform(patch("/orders/{orderId}/pickup", UUID.randomUUID())
                            .with(csrf())
                            .cookie(carrierCookie))
                    .andExpect(status().isNotFound());
        }

        mvc.perform(patch("/orders/{orderId}/pickup", UUID.randomUUID())
                        .with(csrf())
                        .cookie(carrierCookie))
                .andExpect(status().isTooManyRequests());
    }
}
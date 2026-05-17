package com.techstore.app.service;

import com.techstore.app.client.SupabaseAuthClient;
import com.techstore.app.domain.user.Email;
import com.techstore.app.dto.auth.InviteSignupRequest;
import com.techstore.app.dto.auth.RegisterRequest;
import com.techstore.app.dto.auth.RegisterResponse;
import com.techstore.app.dto.auth.SupabaseLoginResponse;
import com.techstore.app.dto.auth.SupabaseUserResponse;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.logger.AuthAuditLogger;
import com.techstore.app.repository.UserRepository;
import com.techstore.app.service.interfaces.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private SupabaseAuthClient supabaseAuthClient;

    @Mock
    private AuthAuditLogger auditLogger;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldRegisterUserAndReturnConfirmationMessage() {
        RegisterRequest request = new RegisterRequest("user@example.com", "Secret123!");
        SupabaseUserResponse user = new SupabaseUserResponse("supabase-id", request.email(), null);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(supabaseAuthClient.signUp(request.email(), request.password(), "customer"))
                .thenReturn(new SupabaseLoginResponse(null, null, null, null, user, null, null));
        RegisterResponse response = authService.register(request, httpRequest);

        assertEquals(request.email(), response.email());
        assertEquals("supabase-id", response.userId());
        assertEquals("Check your email for confirmation link", response.message());
        verify(auditLogger).logRegisterAttempt(request.email(), true, httpRequest);
    }

    @Test
    void shouldLogFailedRegisterAttemptWhenSignupFails() {
        RegisterRequest request = new RegisterRequest("user@example.com", "Secret123!");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        doThrow(new IllegalStateException("boom"))
                .when(supabaseAuthClient)
                .signUp(request.email(), request.password(), "customer");

        assertThrows(IllegalStateException.class, () -> authService.register(request, httpRequest));
        verify(auditLogger).logRegisterAttempt(request.email(), false, httpRequest);
    }

    @Test
    void shouldLogoutAndAuditSuccess() {
        authService.logout("access-token", httpRequest);

        verify(supabaseAuthClient).revokeToken("access-token");
        verify(auditLogger).logLogoutAttempt("unknown", true, httpRequest);
    }

    @Test
    void shouldRejectInviteWithUnsupportedRole() {
        InviteSignupRequest request = new InviteSignupRequest("manager@example.com", "CUSTOMER");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.inviteUser(request, "127.0.0.1", "JUnit"));

        assertEquals("Invalid role: only MANAGER and CARRIER can be invited", exception.getMessage());
        verifyNoInteractions(supabaseAuthClient);
    }

    @Test
    void shouldConfirmInviteAndDefaultMissingRoleToCustomer() throws Exception {
        setWebhookSecret("webhook-secret");

        Map<String, Object> record = new HashMap<>();
        record.put("id", "supabase-id");
        record.put("email", "user@example.com");
        record.put("email_confirmed_at", "2026-05-15T00:00:00Z");
        record.put("raw_user_meta_data", new HashMap<>());

        Map<String, Object> oldRecord = new HashMap<>();
        oldRecord.put("email_confirmed_at", null);

        when(supabaseAuthClient.userExists("supabase-id")).thenReturn(true);
        when(userService.getUserBySupabaseId("supabase-id")).thenReturn(Optional.empty());

        boolean confirmed = authService.confirmInvite("webhook-secret", Map.of(
                "record", record,
                "old_record", oldRecord));

        assertTrue(confirmed);
        verify(userService).registerUser("supabase-id", "user@example.com", "customer");
        verify(userService).confirmUserEmail("supabase-id");
        verify(auditLogger).logConfirmInvite("user@example.com", true, null);
        verify(supabaseAuthClient, never()).deleteUser("supabase-id");
    }

    private void setWebhookSecret(String value) throws Exception {
        Field field = AuthServiceImpl.class.getDeclaredField("webhookSecret");
        field.setAccessible(true);
        field.set(authService, value);
    }
}

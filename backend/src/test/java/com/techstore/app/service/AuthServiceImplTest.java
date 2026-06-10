package com.techstore.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstore.app.client.SupabaseAuthClient;
import com.techstore.app.domain.user.Email;
import com.techstore.app.domain.user.SupabaseUserId;
import com.techstore.app.domain.user.User;
import com.techstore.app.dto.auth.*;
import com.techstore.app.exception.BusinessException;
import com.techstore.app.helpers.CookiesHelper;
import com.techstore.app.logger.AuthAuditLogger;
import com.techstore.app.repository.UserRepository;
import com.techstore.app.service.interfaces.NotificationService;
import com.techstore.app.service.interfaces.UserService;
import com.techstore.app.util.PasswordUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordUtils passwordUtils;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void shouldRegisterUserAndReturnConfirmationMessage() {
        RegisterRequest request = new RegisterRequest("user@example.com", "Secret123!");
        SupabaseUserResponse user = new SupabaseUserResponse("supabase-id", request.email(), null);

        Email email = new Email(request.email());
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(supabaseAuthClient.signUp(request.email(), request.password(), "customer"))
                .thenReturn(new SupabaseLoginResponse(null, null, null, null, user, null, null));
        RegisterResponse response = authService.register(request, httpRequest);

        assertEquals(request.email(), response.email());
        assertEquals("supabase-id", response.userId());
        assertEquals("Check your email for confirmation link", response.message());
        verify(passwordUtils).validate(request.password(), request.email());
        verify(supabaseAuthClient).signUp(request.email(), request.password(), "customer");
        verify(auditLogger).logRegisterAttempt(request.email(), true, httpRequest);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldLogFailedRegisterAttemptWhenSignupFails() {
        RegisterRequest request = new RegisterRequest("user@example.com", "Secret123!");

        Email email = new Email(request.email());
        when(userRepository.existsByEmail(email)).thenReturn(false);
        doThrow(new IllegalStateException("boom"))
                .when(supabaseAuthClient)
                .signUp(request.email(), request.password(), "customer");

        assertThrows(IllegalStateException.class, () -> authService.register(request, httpRequest));

        verify(passwordUtils).validate(request.password(), request.email());
        verify(supabaseAuthClient).signUp(request.email(), request.password(), "customer");
        verify(auditLogger).logRegisterAttempt(request.email(), false, httpRequest);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldLogFailedRegisterAttemptWhenPasswordValidationFails() {
        RegisterRequest request = new RegisterRequest("user@example.com", "password123");

        Email email = new Email(request.email());

        when(userRepository.existsByEmail(email)).thenReturn(false);

        doThrow(new BusinessException("Password is too common"))
                .when(passwordUtils)
                .validate(request.password(), request.email());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(request, httpRequest)
        );

        assertEquals("Password is too common", exception.getMessage());

        verify(passwordUtils).validate(request.password(), request.email());
        verify(supabaseAuthClient, never()).signUp(any(), any(), any());
        verify(auditLogger).logRegisterAttempt(request.email(), false, httpRequest);

        verifyNoInteractions(notificationService);
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

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.inviteUser(request, "127.0.0.1", "JUnit")
        );

        assertEquals("Invalid role: only MANAGER and CARRIER can be invited", exception.getMessage());

        verifyNoInteractions(supabaseAuthClient);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldConfirmInviteAndDefaultMissingRoleToCustomer() throws Exception {
        setWebhookSecret("webhook-secret");

        Map<String, Object> metadata = new HashMap<>();

        Map<String, Object> record = new HashMap<>();
        record.put("id", "supabase-id");
        record.put("email", "user@example.com");
        record.put("email_confirmed_at", "2026-05-15T00:00:00Z");
        record.put("invited_at", "2026-05-14T00:00:00Z");
        record.put("raw_user_meta_data", metadata);

        Map<String, Object> oldRecord = new HashMap<>();
        oldRecord.put("email_confirmed_at", null);

        when(supabaseAuthClient.userExists("supabase-id")).thenReturn(true);
        when(userService.getUserBySupabaseId("supabase-id")).thenReturn(Optional.empty());

        boolean confirmed = authService.confirmInvite("webhook-secret", Map.of(
                "record", record,
                "old_record", oldRecord
        ));

        assertTrue(confirmed);

        verify(userService).registerUser("supabase-id", "user@example.com", "customer");
        verify(userService).confirmUserEmail("supabase-id");

        verify(notificationService).sendEmail(
                eq("user@example.com"),
                eq("TechStore - Registration Completed"),
                contains("Welcome to TechStore")
        );

        verify(auditLogger).logConfirmInvite("user@example.com", true, null);
        verify(supabaseAuthClient, never()).deleteUser("supabase-id");
    }

    @Test
    void shouldConfirmInviteWithProvidedRoleAndSendEmail() throws Exception {
        setWebhookSecret("webhook-secret");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("role", "carrier");

        Map<String, Object> record = new HashMap<>();
        record.put("id", "supabase-id");
        record.put("email", "carrier@example.com");
        record.put("email_confirmed_at", "2026-05-15T00:00:00Z");
        record.put("invited_at", "2026-05-14T00:00:00Z");
        record.put("raw_user_meta_data", metadata);

        Map<String, Object> oldRecord = new HashMap<>();
        oldRecord.put("email_confirmed_at", null);

        when(supabaseAuthClient.userExists("supabase-id")).thenReturn(true);
        when(userService.getUserBySupabaseId("supabase-id")).thenReturn(Optional.empty());

        boolean confirmed = authService.confirmInvite("webhook-secret", Map.of(
                "record", record,
                "old_record", oldRecord
        ));

        assertTrue(confirmed);

        verify(userService).registerUser("supabase-id", "carrier@example.com", "carrier");
        verify(userService).confirmUserEmail("supabase-id");

        verify(notificationService).sendEmail(
                eq("carrier@example.com"),
                eq("TechStore - Registration Completed"),
                contains("Welcome to TechStore")
        );

        verify(auditLogger).logConfirmInvite("carrier@example.com", true, null);
        verify(supabaseAuthClient, never()).deleteUser("supabase-id");
    }

    @Test
    void shouldConfirmInviteEvenWhenRegistrationEmailFails() throws Exception {
        setWebhookSecret("webhook-secret");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("role", "manager");

        Map<String, Object> record = new HashMap<>();
        record.put("id", "supabase-id");
        record.put("email", "manager@example.com");
        record.put("email_confirmed_at", "2026-05-15T00:00:00Z");
        record.put("invited_at", "2026-05-14T00:00:00Z");
        record.put("raw_user_meta_data", metadata);

        Map<String, Object> oldRecord = new HashMap<>();
        oldRecord.put("email_confirmed_at", null);

        when(supabaseAuthClient.userExists("supabase-id")).thenReturn(true);
        when(userService.getUserBySupabaseId("supabase-id")).thenReturn(Optional.empty());

        doThrow(new RuntimeException("SMTP failed"))
                .when(notificationService)
                .sendEmail(anyString(), anyString(), anyString());

        boolean confirmed = authService.confirmInvite("webhook-secret", Map.of(
                "record", record,
                "old_record", oldRecord
        ));

        assertTrue(confirmed);

        verify(userService).registerUser("supabase-id", "manager@example.com", "manager");
        verify(userService).confirmUserEmail("supabase-id");

        verify(notificationService).sendEmail(
                eq("manager@example.com"),
                eq("TechStore - Registration Completed"),
                contains("Welcome to TechStore")
        );

        verify(auditLogger).logConfirmInvite("manager@example.com", true, null);
        verify(supabaseAuthClient, never()).deleteUser("supabase-id");
    }

    @Test
    void shouldUpdatePasswordAndSendEmail() {
        String accessToken = "access-token";
        String newPassword = "StrongPhrase2026!";
        String supabaseUserId = UUID.randomUUID().toString();
        String email = "user@example.com";

        User user = mock(User.class);
        when(user.getEmail()).thenReturn(new Email(email));

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));

        try (MockedStatic<CookiesHelper> cookiesHelper = mockStatic(CookiesHelper.class)) {
            cookiesHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId);

            assertDoesNotThrow(() ->
                    authService.updatePassword(accessToken, newPassword, httpRequest)
            );
        }

        verify(userRepository).findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId));
        verify(passwordUtils).validate(newPassword, email);
        verify(supabaseAuthClient).updatePassword(accessToken, newPassword);

        verify(notificationService).sendEmail(eq(email),
                eq("TechStore - Password Updated Successfully"), contains("Password Updated Successfully"));

        verify(auditLogger).logPasswordUpdate(true, httpRequest);
        verify(auditLogger, never()).logPasswordUpdate(false, httpRequest);
    }

    @Test
    void shouldUpdatePasswordWithoutSendingEmailWhenUserEmailIsNotFound() {
        String accessToken = "access-token";
        String newPassword = "StrongPhrase2026!";
        String supabaseUserId = UUID.randomUUID().toString();

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.empty());

        try (MockedStatic<CookiesHelper> cookiesHelper = mockStatic(CookiesHelper.class)) {
            cookiesHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId);

            assertDoesNotThrow(() ->
                    authService.updatePassword(accessToken, newPassword, httpRequest)
            );
        }

        verify(passwordUtils).validate(newPassword, null);
        verify(supabaseAuthClient).updatePassword(accessToken, newPassword);
        verify(notificationService, never()).sendEmail(anyString(), anyString(), anyString());

        verify(auditLogger).logPasswordUpdate(true, httpRequest);
        verify(auditLogger, never()).logPasswordUpdate(false, httpRequest);
    }

    @Test
    void shouldLogFailedPasswordUpdateWhenPasswordValidationFails() {
        String accessToken = "access-token";
        String newPassword = "weak-password";
        String supabaseUserId = UUID.randomUUID().toString();
        String email = "user@example.com";

        User user = mock(User.class);
        when(user.getEmail()).thenReturn(new Email(email));

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));

        doThrow(new BusinessException("Password is too common"))
                .when(passwordUtils)
                .validate(newPassword, email);

        try (MockedStatic<CookiesHelper> cookiesHelper = mockStatic(CookiesHelper.class)) {
            cookiesHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> authService.updatePassword(accessToken, newPassword, httpRequest)
            );

            assertEquals("Password is too common", exception.getMessage());
        }

        verify(passwordUtils).validate(newPassword, email);
        verify(supabaseAuthClient, never()).updatePassword(anyString(), anyString());
        verify(notificationService, never()).sendEmail(anyString(), anyString(), anyString());

        verify(auditLogger).logPasswordUpdate(false, httpRequest);
        verify(auditLogger, never()).logPasswordUpdate(true, httpRequest);
    }

    @Test
    void shouldLogFailedPasswordUpdateWhenSupabaseUpdateFails() {
        String accessToken = "access-token";
        String newPassword = "StrongPhrase2026!";
        String supabaseUserId = UUID.randomUUID().toString();
        String email = "user@example.com";

        User user = mock(User.class);
        when(user.getEmail()).thenReturn(new Email(email));

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));

        doThrow(new IllegalStateException("Supabase failed"))
                .when(supabaseAuthClient)
                .updatePassword(accessToken, newPassword);

        try (MockedStatic<CookiesHelper> cookiesHelper = mockStatic(CookiesHelper.class)) {
            cookiesHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId);

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> authService.updatePassword(accessToken, newPassword, httpRequest)
            );

            assertEquals("Supabase failed", exception.getMessage());
        }

        verify(passwordUtils).validate(newPassword, email);
        verify(supabaseAuthClient).updatePassword(accessToken, newPassword);
        verify(notificationService, never()).sendEmail(anyString(), anyString(), anyString());

        verify(auditLogger).logPasswordUpdate(false, httpRequest);
        verify(auditLogger, never()).logPasswordUpdate(true, httpRequest);
    }

    @Test
    void shouldUpdatePasswordEvenWhenEmailFails() {
        String accessToken = "access-token";
        String newPassword = "StrongPhrase2026!";
        String supabaseUserId = UUID.randomUUID().toString();
        String email = "user@example.com";

        User user = mock(User.class);
        when(user.getEmail()).thenReturn(new Email(email));

        when(userRepository.findBySupabaseUserId(SupabaseUserId.fromString(supabaseUserId)))
                .thenReturn(Optional.of(user));

        doThrow(new RuntimeException("SMTP failed"))
                .when(notificationService)
                .sendEmail(anyString(), anyString(), anyString());

        try (MockedStatic<CookiesHelper> cookiesHelper = mockStatic(CookiesHelper.class)) {
            cookiesHelper.when(CookiesHelper::getCurrentUserId).thenReturn(supabaseUserId);

            assertDoesNotThrow(() ->
                    authService.updatePassword(accessToken, newPassword, httpRequest)
            );
        }

        verify(passwordUtils).validate(newPassword, email);
        verify(supabaseAuthClient).updatePassword(accessToken, newPassword);
        verify(notificationService).sendEmail(eq(email),
                eq("TechStore - Password Updated Successfully"),
                contains("Password Updated Successfully"));

        verify(auditLogger).logPasswordUpdate(true, httpRequest);
        verify(auditLogger, never()).logPasswordUpdate(false, httpRequest);
    }

    @Test
    void shouldLogFailedMfaEnroll() {
        String accessToken = "access-token";

        when(supabaseAuthClient.enrollTotp(accessToken))
                .thenThrow(new RuntimeException("fail"));


        assertThrows(RuntimeException.class,
                () -> authService.enrollMfa(accessToken));


        verify(auditLogger).logMfaEnrollAttempt("unknown", false);
    }
    @Test
    void shouldEnrollMfaSuccessfullyAndLogAudit() {
        String accessToken = "access-token";
        MfaEnrollResponse response = mock(MfaEnrollResponse.class);

        when(supabaseAuthClient.enrollTotp(accessToken)).thenReturn(response);


        MfaEnrollResponse result = authService.enrollMfa(accessToken);

        assertEquals(response, result);


        verify(supabaseAuthClient).enrollTotp(accessToken);
        verify(auditLogger).logMfaEnrollAttempt("unknown", true);
    }
    @Test
    void shouldCreateMfaChallengeSuccessfully() {
        String accessToken = "access-token";
        MfaChallengeResponse response = mock(MfaChallengeResponse.class);

        when(supabaseAuthClient.createChallenge(accessToken, "factor-1"))
                .thenReturn(response);


        MfaChallengeResponse result = authService.challengeMfa(accessToken, "factor-1");

        assertEquals(response, result);


        verify(auditLogger).logMfaChallengeAttempt("unknown", true);
    }

    @Test
    void shouldLogFailedMfaChallenge() {
        String accessToken = "access-token";

        when(supabaseAuthClient.createChallenge(accessToken, "factor-1"))
                .thenThrow(new RuntimeException("fail"));


        assertThrows(RuntimeException.class,
                () -> authService.challengeMfa(accessToken, "factor-1"));


        verify(auditLogger).logMfaChallengeAttempt("unknown", false);
    }


    @Test
    void shouldVerifyMfaSuccessfully() {
        String accessToken = "access-token";


        authService.verifyMfa(accessToken, "factor-1", "challenge-1", "123456");


        verify(supabaseAuthClient).verifyTotpCode(accessToken, "factor-1", "challenge-1", "123456");
        verify(auditLogger).logMfaVerifyAttempt("unknown", true);
    }

    @Test
    void shouldLogFailedMfaVerify() {
        String accessToken = "access-token";

        doThrow(new RuntimeException("invalid"))
                .when(supabaseAuthClient)
                .verifyTotpCode(any(), any(), any(), any());

        assertThrows(RuntimeException.class,
                () -> authService.verifyMfa(accessToken, "factor-1", "challenge-1", "123456"));


        verify(auditLogger).logMfaVerifyAttempt("unknown", false);
    }
    @Test
    void shouldVerifyChallengeAndSetCookiesAndAuditSuccess() {
        String accessToken = "access-token";
        HttpServletResponse response = mock(HttpServletResponse.class);

        SupabaseLoginResponse upgraded = mock(SupabaseLoginResponse.class);
        when(upgraded.accessToken()).thenReturn("new-token");
        when(upgraded.refreshToken()).thenReturn("refresh-token");

        when(supabaseAuthClient.verifyTotpCode(any(), any(), any(), any()))
                .thenReturn(upgraded);

        authService.verifyChallengeCode(
                accessToken, "factor-1", "challenge-1", "123456", response);


        verify(supabaseAuthClient)
                .verifyTotpCode(accessToken, "factor-1", "challenge-1", "123456");

        verify(auditLogger).logMfaChallengeVerify("unknown", true);
    }

    @Test
    void shouldLogFailedChallengeVerify() {
        String accessToken = "access-token";
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(supabaseAuthClient.verifyTotpCode(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("invalid"));


        assertThrows(RuntimeException.class,
                () -> authService.verifyChallengeCode(
                        accessToken, "factor-1", "challenge-1", "123456", response));


        verify(auditLogger).logMfaChallengeVerify("unknown", false);
    }
    @Test
    void shouldUnenrollMfaSuccessfully() {
        String accessToken = "access-token";


        authService.unenrollMfa(accessToken, "factor-1");


        verify(supabaseAuthClient).unenrollFactor(accessToken, "factor-1");
        verify(auditLogger).logMfaUnenroll("unknown", true);
    }

    @Test
    void shouldLogFailedMfaUnenroll() {
        String accessToken = "access-token";

        doThrow(new RuntimeException("fail"))
                .when(supabaseAuthClient)
                .unenrollFactor(any(), any());


        assertThrows(RuntimeException.class,
                () -> authService.unenrollMfa(accessToken, "factor-1"));


        verify(auditLogger).logMfaUnenroll("unknown", false);
    }

    private void setWebhookSecret(String value) throws Exception {
        Field field = AuthServiceImpl.class.getDeclaredField("webhookSecret");
        field.setAccessible(true);
        field.set(authService, value);
    }
}
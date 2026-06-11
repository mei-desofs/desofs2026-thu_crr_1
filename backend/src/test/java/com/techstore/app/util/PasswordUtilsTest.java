package com.techstore.app.util;

import com.techstore.app.client.HaveIBeenPwnedClient;
import com.techstore.app.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordUtilsTest {

    @Mock
    private HaveIBeenPwnedClient hibpClient;

    private PasswordUtils passwordUtils;

    @BeforeEach
    void setUp() {
        passwordUtils = new PasswordUtils(hibpClient);

        ReflectionTestUtils.setField(passwordUtils, "commonPasswords", Set.of(
                        "verycommonphrase",
                        "qwerty123456",
                        "letmein123456"));
    }

    @Test
    void shouldAcceptValidPassword() {
        when(hibpClient.isBreached("StrongUniquePass2026!")).thenReturn(false);

        assertDoesNotThrow(() -> passwordUtils.validate("StrongUniquePass2026!", "user@example.com"));

        verify(hibpClient).isBreached("StrongUniquePass2026!");
    }

    @Test
    void shouldRejectNullPassword() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> passwordUtils.validate(null, "user@example.com"));

        assertEquals("Password must be at least 12 characters long.", exception.getMessage());

        verifyNoInteractions(hibpClient);
    }

    @Test
    void shouldRejectShortPassword() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> passwordUtils.validate("Short1!", "user@example.com"));

        assertEquals("Password must be at least 12 characters long.", exception.getMessage());

        verifyNoInteractions(hibpClient);
    }

    @Test
    void shouldRejectPasswordContainingContextWord() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> passwordUtils.validate("TechStore2026!", "user@example.com"));

        assertEquals("Password must not contain easily guessable words.", exception.getMessage());

        verifyNoInteractions(hibpClient);
    }

    @Test
    void shouldRejectPasswordContainingAdminContextWord() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> passwordUtils.validate("AdminStrong2026!", "user@example.com"));

        assertEquals("Password must not contain easily guessable words.", exception.getMessage());

        verifyNoInteractions(hibpClient);
    }

    @Test
    void shouldRejectPasswordContainingEmailLocalPart() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> passwordUtils.validate("MiguelStrong2026!", "miguel@example.com"));

        assertEquals("Password must not contain parts of your email address.", exception.getMessage());

        verifyNoInteractions(hibpClient);
    }

    @Test
    void shouldNotRejectEmailLocalPartWhenShorterThanFourCharacters() {
        when(hibpClient.isBreached("AbcStrongPass2026!")).thenReturn(false);

        assertDoesNotThrow(() -> passwordUtils.validate("AbcStrongPass2026!", "abc@example.com"));

        verify(hibpClient).isBreached("AbcStrongPass2026!");
    }

    @Test
    void shouldRejectCommonPassword() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> passwordUtils.validate("Qwerty123456", "client@example.com"));

        assertEquals("Password is too common. Please choose a more unique password.", exception.getMessage());

        verifyNoInteractions(hibpClient);
    }

    @Test
    void shouldRejectBreachedPassword() {
        when(hibpClient.isBreached("StrongButBreached2026!")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> passwordUtils.validate("StrongButBreached2026!", "user@example.com")
        );

        assertEquals("This password has appeared in a known data breach. Please choose a different password.",
                exception.getMessage());

        verify(hibpClient).isBreached("StrongButBreached2026!");
    }

    @Test
    void shouldCheckCommonPasswordsCaseInsensitive() {
        BusinessException exception = assertThrows(BusinessException.class,
            () -> passwordUtils.validate("VERYCOMMONPHRASE", "client@example.com"));

        assertEquals("Password is too common. Please choose a more unique password.", exception.getMessage());

        verifyNoInteractions(hibpClient);
    }
}

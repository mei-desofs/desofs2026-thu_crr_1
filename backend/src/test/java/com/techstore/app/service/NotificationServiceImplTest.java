package com.techstore.app.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(mailSender);

        ReflectionTestUtils.setField(notificationService, "fromEmail", "no-reply@techstore.com");
        ReflectionTestUtils.setField(notificationService, "fromName", "TechStore");
    }

    @Test
    void shouldSendEmail() {
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> notificationService.sendEmail(
                "customer@example.com",
                "TechStore - Order Confirmation #123",
                "<p>Your order was created.</p>"
        ));

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenMailSenderFails() {
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        org.mockito.Mockito.doThrow(new MailSendException("SMTP unavailable"))
                .when(mailSender)
                .send(mimeMessage);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                notificationService.sendEmail(
                        "customer@example.com",
                        "TechStore - Order Confirmation #123",
                        "<p>Your order was created.</p>"
                )
        );

        assertEquals("Error sending email", exception.getMessage());

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenCreateMimeMessageFails() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Could not create message"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                notificationService.sendEmail(
                        "customer@example.com",
                        "TechStore - Order Confirmation #123",
                        "<p>Your order was created.</p>"
                )
        );

        assertEquals("Error sending email", exception.getMessage());

        verify(mailSender).createMimeMessage();
    }
}
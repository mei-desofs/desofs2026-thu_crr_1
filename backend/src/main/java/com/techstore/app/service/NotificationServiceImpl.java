package com.techstore.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.techstore.app.service.interfaces.NotificationService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from.email}")
    private String fromEmail;

    @Value("${spring.mail.from.name}")
    private String fromName;

    @Override
    public void sendOrderConfirmationEmail(String to, String subject, String body) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(buildEmailTemplate(body), true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error sending email", e);
        }
    }

    private String buildEmailTemplate(String body) {
        return """
                <html>
                    <body style="margin:0; padding:0; background-color:#f5f7fb; font-family:Arial, sans-serif;">

                        <div style="max-width:600px; margin:40px auto; background:#ffffff; border-radius:8px; overflow:hidden; border:1px solid #e6e9f0;">

                            <div style="background:#1e66f5; padding:20px; text-align:center;">
                                <h1 style="color:#ffffff; margin:0;">%s</h1>
                            </div>

                            <div style="padding:30px; color:#333;">
                                <h2 style="color:#1e66f5;">Order Confirmation</h2>

                                <p>Hello,</p>

                                <p>%s</p>

                                <hr style="border:none; border-top:1px solid #eee; margin:20px 0;" />

                                <p style="font-size:12px; color:#888;">
                                    This is an automated message. Please do not reply.
                                </p>
                            </div>

                        </div>

                    </body>
                </html>
                """
                .formatted(fromName, body, fromName);
    }
}
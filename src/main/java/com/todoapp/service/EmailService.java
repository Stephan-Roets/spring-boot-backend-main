package com.todoapp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(
        EmailService.class
    );

    private final JavaMailSender mailSender;
    private final String frontendUrl;

    public EmailService(
        JavaMailSender mailSender,
        @Value("${app.frontend.url}") String frontendUrl
    ) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
    }

    @Async
    public void sendVerificationEmail(String to, String name, String token) {
        String verifyUrl = frontendUrl + "/verify-email?token=" + token;
        String subject = "Verify your email - ToDo App";
        String body = buildVerificationEmailHtml(name, verifyUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message,
                true,
                "UTF-8"
            );
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.info("Verification email sent to {}", to);
        } catch (MessagingException e) {
            log.warn(
                "Failed to send verification email to {} — SMTP error: {}",
                to,
                e.getMessage()
            );
        } catch (Exception e) {
            log.warn(
                "Failed to send verification email to {} — {}: {}",
                to,
                e.getClass().getSimpleName(),
                e.getMessage()
            );
        }
    }

    private String buildVerificationEmailHtml(String name, String verifyUrl) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }
                .container { max-width: 600px; margin: 0 auto; padding: 40px 20px; }
                .btn { display: inline-block; padding: 14px 32px; background-color: #0F172A;
                       color: #ffffff; text-decoration: none; border-radius: 8px; font-weight: 600; }
                .footer { margin-top: 32px; color: #64748B; font-size: 14px; }
            </style>
        </head>
        <body>
            <div class="container">
                <h1 style="color: #0F172A;">Welcome to ToDo App!</h1>
                <p>Hi %s,</p>
                <p>Thank you for signing up. Please verify your email address by clicking the button below.
                   This link will expire in <strong>30 minutes</strong>.</p>
                <p style="margin: 32px 0;">
                    <a href="%s" class="btn">Verify Email Address</a>
                </p>
                <p>If you didn't create an account, you can safely ignore this email.</p>
                <div class="footer">
                    <p>If the button doesn't work, copy and paste this link into your browser:</p>
                    <p style="word-break: break-all;">%s</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(name, verifyUrl, verifyUrl);
    }
}

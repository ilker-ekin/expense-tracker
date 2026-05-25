package com.expense.tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:noreply@vault.app}")
    private String fromAddress;

    @Async
    public void sendVerificationEmail(String to, String token) {
        String link = baseUrl + "/api/auth/verify?token=" + token;
        String html = """
                <div style="font-family:sans-serif;max-width:480px;margin:0 auto;padding:32px">
                  <h2 style="color:#6366f1">Verify your email</h2>
                  <p>Click the button below to verify your email address and activate your Vault account.</p>
                  <a href="%s" style="display:inline-block;padding:12px 28px;background:#6366f1;color:#fff;
                     text-decoration:none;border-radius:8px;font-weight:600;margin:16px 0">Verify Email</a>
                  <p style="font-size:13px;color:#888">This link expires in 24 hours. If you didn't create an account, ignore this email.</p>
                </div>
                """.formatted(link);
        sendHtml(to, "Verify your Vault account", html);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        String link = baseUrl + "/?reset=" + token;
        String html = """
                <div style="font-family:sans-serif;max-width:480px;margin:0 auto;padding:32px">
                  <h2 style="color:#6366f1">Reset your password</h2>
                  <p>Click the button below to set a new password for your Vault account.</p>
                  <a href="%s" style="display:inline-block;padding:12px 28px;background:#6366f1;color:#fff;
                     text-decoration:none;border-radius:8px;font-weight:600;margin:16px 0">Reset Password</a>
                  <p style="font-size:13px;color:#888">This link expires in 1 hour. If you didn't request this, ignore this email.</p>
                </div>
                """.formatted(link);
        sendHtml(to, "Reset your Vault password", html);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}

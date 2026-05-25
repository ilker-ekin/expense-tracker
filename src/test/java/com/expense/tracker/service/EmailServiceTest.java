package com.expense.tracker.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @Mock MimeMessage mimeMessage;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(emailService, "fromAddress", "test@vault.app");
    }

    @Test
    void sendVerificationEmail_callsMailSender() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendVerificationEmail("user@test.com", "abc123");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_callsMailSender() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPasswordResetEmail("user@test.com", "reset-token");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendVerificationEmail_mailSenderThrows_doesNotPropagate() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP down"));

        assertThatCode(() -> emailService.sendVerificationEmail("user@test.com", "abc123"))
                .doesNotThrowAnyException();
    }

    @Test
    void sendPasswordResetEmail_mailSenderThrows_doesNotPropagate() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP down"));

        assertThatCode(() -> emailService.sendPasswordResetEmail("user@test.com", "token"))
                .doesNotThrowAnyException();
    }
}
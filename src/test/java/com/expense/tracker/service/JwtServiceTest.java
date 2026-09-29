package com.expense.tracker.service;

import com.expense.tracker.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application.properties")
class JwtServiceTest {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expirationMs", expirationMs);
    }

    private User testUser(String email) {
        return User.builder()
                .id(1L).email(email).password("hashed").fullName("Test User").role("USER")
                .build();
    }

    @Test
    void generateToken_returnsNonBlankToken() {
        assertThat(jwtService.generateToken(testUser("user@example.com"))).isNotBlank();
    }

    @Test
    void extractEmail_returnsSubjectFromToken() {
        User user = testUser("user@example.com");
        assertThat(jwtService.extractEmail(jwtService.generateToken(user))).isEqualTo("user@example.com");
    }

    @Test
    void isTokenValid_validTokenAndMatchingUser_returnsTrue() {
        User user = testUser("user@example.com");
        assertThat(jwtService.isTokenValid(jwtService.generateToken(user), user)).isTrue();
    }

    @Test
    void isTokenValid_tokenForDifferentUser_returnsFalse() {
        String token = jwtService.generateToken(testUser("alice@example.com"));
        assertThat(jwtService.isTokenValid(token, testUser("bob@example.com"))).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        JwtService expiredService = new JwtService();
        ReflectionTestUtils.setField(expiredService, "secret", secret);
        ReflectionTestUtils.setField(expiredService, "expirationMs", -1L);

        User user = testUser("user@example.com");
        String token = expiredService.generateToken(user);
        assertThat(expiredService.isTokenValid(token, user)).isFalse();
    }

    @Test
    void getExpirationMs_returnsConfiguredValue() {
        assertThat(jwtService.getExpirationMs()).isEqualTo(expirationMs);
    }

    @Test
    void validateSecret_configuredSecret_passes() {
        assertThatCode(jwtService::validateSecret).doesNotThrowAnyException();
    }

    @Test
    void validateSecret_emptySecret_throws() {
        ReflectionTestUtils.setField(jwtService, "secret", "");
        assertThatThrownBy(jwtService::validateSecret)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jwt.secret");
    }
}

package com.expense.tracker.service;

import com.expense.tracker.dto.LoginRequest;
import com.expense.tracker.dto.RegisterRequest;
import com.expense.tracker.dto.ResetPasswordRequest;
import com.expense.tracker.entity.AuthToken;
import com.expense.tracker.entity.AuthToken.TokenType;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.AuthTokenRepository;
import com.expense.tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock AuthTokenRepository authTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock EmailService emailService;

    @InjectMocks UserService userService;

    private User savedUser() {
        return User.builder()
                .id(1L)
                .email("user@example.com")
                .password("$2a$hashed")
                .fullName("Test User")
                .role("USER")
                .emailVerified(true)
                .build();
    }

    private User unverifiedUser() {
        return User.builder()
                .id(2L)
                .email("new@example.com")
                .password("$2a$hashed")
                .fullName("New User")
                .role("USER")
                .emailVerified(false)
                .build();
    }

    // --- register ---

    @Test
    void register_newEmail_savesUserAndSendsVerification() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password1")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser());
        when(authTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User user = userService.register(
                new RegisterRequest("user@example.com", "password1", "Test User"));

        assertThat(user.getEmail()).isEqualTo("user@example.com");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(eq("user@example.com"), anyString());
    }

    @Test
    void register_duplicateEmail_throwsIllegalArgument() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                userService.register(new RegisterRequest("user@example.com", "password1", "Test User")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_passwordIsEncoded() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("$2a$hashed");
        when(userRepository.save(any())).thenReturn(savedUser());
        when(authTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userService.register(new RegisterRequest("user@example.com", "plaintext", "Test User"));

        verify(passwordEncoder).encode("plaintext");
        verify(userRepository).save(argThat(u -> "$2a$hashed".equals(u.getPassword())));
    }

    @Test
    void register_roleIsAlwaysUser() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(savedUser());
        when(authTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userService.register(new RegisterRequest("user@example.com", "password1", "Test User"));

        verify(userRepository).save(argThat(u -> "USER".equals(u.getRole())));
    }

    // --- login ---

    @Test
    void login_verifiedUser_returnsUser() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(savedUser()));

        User user = userService.login(
                new LoginRequest("user@example.com", "password1"));

        assertThat(user.getEmail()).isEqualTo("user@example.com");
        verify(authenticationManager).authenticate(
                argThat(a -> a instanceof UsernamePasswordAuthenticationToken));
    }

    @Test
    void login_unverifiedUser_throwsIllegalState() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(unverifiedUser()));

        assertThatThrownBy(() ->
                userService.login(new LoginRequest("new@example.com", "password1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not verified");
    }

    @Test
    void login_badCredentials_throwsBadCredentials() {
        doThrow(new BadCredentialsException("bad"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() ->
                userService.login(new LoginRequest("user@example.com", "wrongpass")))
                .isInstanceOf(BadCredentialsException.class);
    }

    // --- loadUserByUsername ---

    @Test
    void loadUserByUsername_existingEmail_returnsUser() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(savedUser()));

        var result = userService.loadUserByUsername("user@example.com");

        assertThat(result.getUsername()).isEqualTo("user@example.com");
    }

    @Test
    void loadUserByUsername_unknownEmail_throwsUsernameNotFound() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("ghost@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // --- verifyEmail ---

    private AuthToken validVerificationToken(User user) {
        return AuthToken.builder()
                .id(1L).user(user).token("verify-token").type(TokenType.VERIFICATION)
                .expiresAt(LocalDateTime.now().plusHours(24)).build();
    }

    @Test
    void verifyEmail_validToken_setsVerifiedAndMarksUsed() {
        User user = unverifiedUser();
        AuthToken token = validVerificationToken(user);
        when(authTokenRepository.findByTokenAndType("verify-token", TokenType.VERIFICATION))
                .thenReturn(Optional.of(token));

        userService.verifyEmail("verify-token");

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(token.getUsedAt()).isNotNull();
        verify(userRepository).save(user);
        verify(authTokenRepository).save(token);
    }

    @Test
    void verifyEmail_invalidToken_throwsIllegalArgument() {
        when(authTokenRepository.findByTokenAndType("bad", TokenType.VERIFICATION))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.verifyEmail("bad"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void verifyEmail_expiredToken_throwsIllegalArgument() {
        User user = unverifiedUser();
        AuthToken token = AuthToken.builder()
                .id(1L).user(user).token("expired").type(TokenType.VERIFICATION)
                .expiresAt(LocalDateTime.now().minusHours(1)).build();
        when(authTokenRepository.findByTokenAndType("expired", TokenType.VERIFICATION))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> userService.verifyEmail("expired"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired");
    }

    // --- resendVerification ---

    @Test
    void resendVerification_unverifiedUser_sendsEmail() {
        User user = unverifiedUser();
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(user));
        when(authTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userService.resendVerification("new@example.com");

        verify(emailService).sendVerificationEmail(eq("new@example.com"), anyString());
    }

    @Test
    void resendVerification_unknownEmail_doesNothing() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        userService.resendVerification("ghost@example.com");

        verify(emailService, never()).sendVerificationEmail(any(), any());
    }

    // --- forgotPassword ---

    @Test
    void forgotPassword_existingUser_sendsResetEmail() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(savedUser()));
        when(authTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userService.forgotPassword("user@example.com");

        verify(emailService).sendPasswordResetEmail(eq("user@example.com"), anyString());
    }

    @Test
    void forgotPassword_unknownEmail_doesNothing() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        userService.forgotPassword("ghost@example.com");

        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    // --- resetPassword ---

    @Test
    void resetPassword_validToken_updatesPasswordAndMarksUsed() {
        User user = savedUser();
        AuthToken token = AuthToken.builder()
                .id(2L).user(user).token("reset-token").type(TokenType.PASSWORD_RESET)
                .expiresAt(LocalDateTime.now().plusHours(1)).build();
        when(authTokenRepository.findByTokenAndType("reset-token", TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newpass123")).thenReturn("$2a$encoded");

        userService.resetPassword(new ResetPasswordRequest("reset-token", "newpass123"));

        assertThat(user.getPassword()).isEqualTo("$2a$encoded");
        assertThat(token.getUsedAt()).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_invalidToken_throwsIllegalArgument() {
        when(authTokenRepository.findByTokenAndType("bad", TokenType.PASSWORD_RESET))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.resetPassword(new ResetPasswordRequest("bad", "newpass123")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

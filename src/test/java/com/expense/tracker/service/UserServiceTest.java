package com.expense.tracker.service;

import com.expense.tracker.dto.LoginRequest;
import com.expense.tracker.dto.RegisterRequest;
import com.expense.tracker.entity.User;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks UserService userService;

    private User savedUser() {
        return User.builder()
                .id(1L)
                .email("user@example.com")
                .password("$2a$hashed")
                .fullName("Test User")
                .role("USER")
                .build();
    }

    // --- register ---

    @Test
    void register_newEmail_savesUserAndReturnsUser() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password1")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser());

        User user = userService.register(
                new RegisterRequest("user@example.com", "password1", "Test User"));

        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getFullName()).isEqualTo("Test User");
        verify(userRepository).save(any(User.class));
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

        userService.register(new RegisterRequest("user@example.com", "plaintext", "Test User"));

        verify(passwordEncoder).encode("plaintext");
        verify(userRepository).save(argThat(u -> "$2a$hashed".equals(u.getPassword())));
    }

    @Test
    void register_roleIsAlwaysUser() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(savedUser());

        userService.register(new RegisterRequest("user@example.com", "password1", "Test User"));

        verify(userRepository).save(argThat(u -> "USER".equals(u.getRole())));
    }

    // --- login ---

    @Test
    void login_validCredentials_returnsUser() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(savedUser()));

        User user = userService.login(
                new LoginRequest("user@example.com", "password1"));

        assertThat(user.getEmail()).isEqualTo("user@example.com");
        verify(authenticationManager).authenticate(
                argThat(a -> a instanceof UsernamePasswordAuthenticationToken));
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
}

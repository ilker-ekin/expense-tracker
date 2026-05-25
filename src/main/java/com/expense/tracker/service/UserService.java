package com.expense.tracker.service;

import com.expense.tracker.dto.LoginRequest;
import com.expense.tracker.dto.RegisterRequest;
import com.expense.tracker.dto.ResetPasswordRequest;
import com.expense.tracker.dto.UpdateProfileRequest;
import com.expense.tracker.dto.UserProfileResponse;
import com.expense.tracker.entity.AuthToken;
import com.expense.tracker.entity.AuthToken.TokenType;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.AuthTokenRepository;
import com.expense.tracker.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public UserService(
            UserRepository userRepository,
            AuthTokenRepository authTokenRepository,
            PasswordEncoder passwordEncoder,
            @Lazy AuthenticationManager authenticationManager,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role("USER")
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        String token = createToken(user, TokenType.VERIFICATION, 24);
        emailService.sendVerificationEmail(user.getEmail(), token);

        return user;
    }

    public User login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.isEmailVerified()) {
            throw new IllegalStateException("Email not verified. Please check your inbox.");
        }

        return user;
    }

    @Transactional
    public void verifyEmail(String token) {
        AuthToken authToken = validateToken(token, TokenType.VERIFICATION);
        User user = authToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        authToken.setUsedAt(LocalDateTime.now());
        authTokenRepository.save(authToken);
    }

    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.isEmailVerified()) return;

        String token = createToken(user, TokenType.VERIFICATION, 24);
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return;

        String token = createToken(user, TokenType.PASSWORD_RESET, 1);
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        AuthToken authToken = validateToken(request.token(), TokenType.PASSWORD_RESET);
        User user = authToken.getUser();
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        authToken.setUsedAt(LocalDateTime.now());
        authTokenRepository.save(authToken);
    }

    public UserProfileResponse updateProfile(User user, UpdateProfileRequest request) {
        user.setFullName(request.fullName());
        userRepository.save(user);
        return new UserProfileResponse(user.getEmail(), user.getFullName());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private String createToken(User user, TokenType type, int expirationHours) {
        String token = UUID.randomUUID().toString();
        AuthToken authToken = AuthToken.builder()
                .user(user)
                .token(token)
                .type(type)
                .expiresAt(LocalDateTime.now().plusHours(expirationHours))
                .build();
        authTokenRepository.save(authToken);
        return token;
    }

    private AuthToken validateToken(String token, TokenType type) {
        AuthToken authToken = authTokenRepository.findByTokenAndType(token, type)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));
        if (authToken.isUsed()) {
            throw new IllegalArgumentException("Token has already been used");
        }
        if (authToken.isExpired()) {
            throw new IllegalArgumentException("Token has expired");
        }
        return authToken;
    }
}

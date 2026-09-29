package com.expense.tracker.config;

import com.expense.tracker.entity.User;
import com.expense.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates a verified demo account for local development. Only active with the {@code dev} profile,
 * so the known credentials never reach other environments.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    static final String DEMO_EMAIL = "demo@vault.dev";
    static final String DEMO_PASSWORD = "demo1234";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(DEMO_EMAIL)) return;

        userRepository.save(User.builder()
                .email(DEMO_EMAIL)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .fullName("Demo User")
                .role("USER")
                .emailVerified(true)
                .build());
        log.info("Seeded dev demo account {}", DEMO_EMAIL);
    }
}

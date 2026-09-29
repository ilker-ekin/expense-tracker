package com.expense.tracker.config;

import com.expense.tracker.entity.User;
import com.expense.tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevDataSeederTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks DevDataSeeder seeder;

    @Test
    void isRestrictedToDevProfile() {
        assertThat(DevDataSeeder.class.getAnnotation(Profile.class).value()).containsExactly("dev");
    }

    @Test
    void run_demoUserMissing_createsVerifiedUser() {
        when(userRepository.existsByEmail(DevDataSeeder.DEMO_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(DevDataSeeder.DEMO_PASSWORD)).thenReturn("hashed");

        seeder.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(DevDataSeeder.DEMO_EMAIL);
        assertThat(saved.getPassword()).isEqualTo("hashed");
        assertThat(saved.isEmailVerified()).isTrue();
    }

    @Test
    void run_demoUserExists_doesNothing() {
        when(userRepository.existsByEmail(DevDataSeeder.DEMO_EMAIL)).thenReturn(true);

        seeder.run();

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }
}

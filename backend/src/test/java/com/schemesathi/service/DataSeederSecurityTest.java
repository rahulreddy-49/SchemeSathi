package com.schemesathi.service;

import com.schemesathi.entity.User;
import com.schemesathi.repository.CitizenProfileRepository;
import com.schemesathi.repository.SchemeRepository;
import com.schemesathi.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederSecurityTest {

    @Mock
    private SchemeRepository schemeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CitizenProfileRepository citizenProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("In production environment, demo users must NOT be seeded")
    void testProductionDoesNotSeedDemoUsers() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        when(schemeRepository.count()).thenReturn(10L);

        DataSeeder seeder = new DataSeeder(
                schemeRepository,
                userRepository,
                citizenProfileRepository,
                passwordEncoder,
                env,
                false // app.seed.demo-users=false
        );

        seeder.run();

        // Verify that userRepository was never queried or populated with demo users
        verify(userRepository, never()).existsByEmail("admin@schemesathi.in");
        verify(userRepository, never()).existsByEmail("citizen@schemesathi.in");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("In explicit dev/demo environment with empty database, demo users are seeded")
    void testDemoEnvironmentSeedsUsersWhenEmpty() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        when(userRepository.existsByEmail("admin@schemesathi.in")).thenReturn(false);
        when(userRepository.existsByEmail("citizen@schemesathi.in")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(schemeRepository.count()).thenReturn(10L);

        DataSeeder seeder = new DataSeeder(
                schemeRepository,
                userRepository,
                citizenProfileRepository,
                passwordEncoder,
                env,
                true // app.seed.demo-users=true
        );

        seeder.run();

        verify(userRepository).existsByEmail("admin@schemesathi.in");
        verify(userRepository).existsByEmail("citizen@schemesathi.in");
        verify(userRepository, atLeast(2)).save(any(User.class));
    }
}

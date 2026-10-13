package com.schemesathi.service;

import com.schemesathi.dto.AuthDto;
import com.schemesathi.entity.User;
import com.schemesathi.repository.CitizenProfileRepository;
import com.schemesathi.repository.UserRepository;
import com.schemesathi.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@SuppressWarnings("null")
public class AuthService {

    private final UserRepository userRepository;
    private final CitizenProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       CitizenProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        String cleanEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        if (cleanEmail.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("An account with email '" + cleanEmail + "' is already registered. Please sign in instead.");
        }

        User user = User.builder()
                .email(cleanEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName() != null ? request.getFullName().trim() : "")
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .role(User.UserRole.CITIZEN)
                .build();

        user = userRepository.save(user);
        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name());

        return AuthDto.AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .hasProfile(false)
                .build();
    }

    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        String cleanEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        if (cleanEmail.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("No account found with email '" + cleanEmail + "'. Please click 'Register as a Citizen' below to create one."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password for '" + cleanEmail + "'. Please check your password and try again.");
        }

        if (user.getActive() != null && !user.getActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        boolean hasProfile = profileRepository.findByUserId(user.getId()).isPresent();
        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name());

        return AuthDto.AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .hasProfile(hasProfile)
                .build();
    }

    public User getCurrentUser(String email) {
        String cleanEmail = email != null ? email.trim().toLowerCase() : "";
        return userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}

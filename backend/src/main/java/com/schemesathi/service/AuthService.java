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
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone())
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
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!user.getActive()) {
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
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}

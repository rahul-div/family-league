package com.familyleague.auth.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.familyleague.auth.dto.AuthResponse;
import com.familyleague.auth.dto.LoginRequest;
import com.familyleague.auth.dto.RegisterRequest;
import com.familyleague.auth.security.JwtTokenProvider;
import com.familyleague.auth.security.SecurityUser;
import com.familyleague.common.exception.ConflictException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.notification.entity.NotificationEventType;
import com.familyleague.notification.service.EmailNotificationService;
import com.familyleague.user.dto.UserResponse;
import com.familyleague.user.entity.User;
import com.familyleague.user.entity.UserRole;
import com.familyleague.user.repository.UserRepository;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailNotificationService emailNotificationService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           EmailNotificationService emailNotificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        return doRegister(request, UserRole.USER);
    }

    @Override
    public AuthResponse createAdmin(RegisterRequest request) {
        return doRegister(request, UserRole.ADMIN);
    }

    private AuthResponse doRegister(RegisterRequest request, UserRole role) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new ConflictException("Email already registered: " + request.email());
        }
        if (userRepository.existsByUsername(request.username().toLowerCase())) {
            throw new ConflictException("Username already taken: " + request.username());
        }

        User user = User.builder()
                .username(request.username().toLowerCase())
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName() != null ? request.displayName() : request.username())
                .role(role)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        emailNotificationService.queueEmail(
                user.getEmail(), user.getId(),
                "Welcome to Family League!",
                "Hi " + user.getDisplayName() + ", welcome to Family League! Start predicting now.",
                NotificationEventType.WELCOME, null
        );

        String token = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), "ROLE_" + user.getRole().name());

        return new AuthResponse(token, user.getId(), user.getUsername(),
                user.getEmail(), user.getRole().name());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailOrUsername(request.usernameOrEmail().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), "ROLE_" + user.getRole().name());

        return new AuthResponse(token, user.getId(), user.getUsername(),
                user.getEmail(), user.getRole().name());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken() {
        java.util.UUID userId = SecurityUser.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        String token = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), "ROLE_" + user.getRole().name());

        return new AuthResponse(token, user.getId(), user.getUsername(),
                user.getEmail(), user.getRole().name());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        java.util.UUID userId = SecurityUser.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        return UserResponse.from(user);
    }
}

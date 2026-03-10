package com.todoapp.service;

import com.todoapp.dto.*;
import com.todoapp.exception.BadRequestException;
import com.todoapp.exception.UnauthorizedException;
import com.todoapp.model.User;
import com.todoapp.model.VerificationToken;
import com.todoapp.repository.UserRepository;
import com.todoapp.repository.VerificationTokenRepository;
import com.todoapp.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            VerificationTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
    }

    @Transactional
    public UserDto signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .department(request.getDepartment())
                .bio(request.getBio())
                .role(User.Role.USER)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        // Generate verification token (30 min expiry)
        String token = jwtTokenProvider.generateVerificationToken(user.getId(), user.getEmail());

        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(OffsetDateTime.now().plusMinutes(30))
                .used(false)
                .build();

        tokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), token);

        return UserDto.fromEntity(user);
    }

    @Transactional
    public String verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        if (verificationToken.getUsed()) {
            throw new BadRequestException("This verification link has already been used");
        }

        if (verificationToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BadRequestException("This verification link has expired. Please sign up again.");
        }

        // Mark token as used
        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        // Verify user email
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        return "Email verified successfully. You can now log in.";
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.getEmailVerified()) {
            throw new UnauthorizedException("Please verify your email before logging in. Check your inbox.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .user(UserDto.fromEntity(user))
                .build();
    }

    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new BadRequestException("No account found with this email"));

        if (user.getEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        String token = jwtTokenProvider.generateVerificationToken(user.getId(), user.getEmail());

        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(OffsetDateTime.now().plusMinutes(30))
                .used(false)
                .build();

        tokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), token);
    }
}

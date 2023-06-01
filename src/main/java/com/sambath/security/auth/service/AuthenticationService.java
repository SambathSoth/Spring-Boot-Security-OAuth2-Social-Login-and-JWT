package com.sambath.security.auth.service;

import com.sambath.security.auth.dto.AuthenticationRequest;
import com.sambath.security.auth.dto.AuthenticationResponse;
import com.sambath.security.auth.dto.JwtToken;
import com.sambath.security.auth.dto.RegisterRequest;
import com.sambath.security.exception.BadRequestException;
import com.sambath.security.exception.EmailAlreadyExistsException;
import com.sambath.security.exception.ResourceNotFoundException;
import com.sambath.security.confirmationToken.ConfirmationToken;
import com.sambath.security.auth.dto.SuccessResponse;
import com.sambath.security.confirmationToken.ConfirmationTokenService;
import com.sambath.security.email.EmailSenderService;
import com.sambath.security.jwt.JwtService;
import com.sambath.security.jwt.JwtUtil;
import com.sambath.security.user.dto.RefreshTokenRequest;
import com.sambath.security.user.dto.RefreshTokenResponse;
import com.sambath.security.user.dto.UserResponse;
import com.sambath.security.user.entity.AuthProvider;
import com.sambath.security.user.entity.RefreshToken;
import com.sambath.security.user.entity.Role;
import com.sambath.security.user.entity.User;
import com.sambath.security.user.repository.RefreshTokenRepository;
import com.sambath.security.user.repository.UserRepository;
import com.sambath.security.user.service.UserService;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailSenderService emailSenderService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ConfirmationTokenService confirmationTokenService;
    @Value("${app.auth.jwt.refresh-token-rotation}")
    private boolean JWT_REFRESH_TOKEN_ROTATION;
    private final static Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    // Register user
    public SuccessResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            LOGGER.error("Email already exists");
            throw new EmailAlreadyExistsException("Email already exists");
        }

        // Create user object
        var user = User.builder()
                .name(request.getFirstName() + " " + request.getLastName())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.email)
                .role(Role.USER)
                .build();
        // Save user to database
        userRepository.save(user);

        // Generate Confirmation token
        String token = UUID.randomUUID().toString();
        var confirmationToken = ConfirmationToken.builder()
                .token(token)
                .createdAt(java.time.LocalDateTime.now())
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        // Save confirmation token to database
        confirmationTokenService.saveConfirmationToken(confirmationToken);

//        // Send confirmation token to user email
//        String link = "http://localhost:8080/api/v1/registration/confirm?token=" + token;
//        emailSenderService.send(
//                request.getEmail(),
//                confirmEmailTemplate(user.getFirstName(), link)
//        );

        return SuccessResponse.builder()
                .message("User registered successfully. Please check your email to confirm registration.")
                .build();
    }

    // Authenticate user
    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        // Authenticate user with email and password
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            if (e.getMessage().equals("Bad credentials")) {
                LOGGER.error("Incorrect username or password");
                throw new BadRequestException("Incorrect username or password");
            }
            LOGGER.error(e.getMessage());
            throw new BadRequestException(e.getMessage());
        }

        // Find user by email
        var user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        return generateAuthenticationResponse(user);
    }

    // Generate AuthenticationResponse
    public AuthenticationResponse generateAuthenticationResponse(User user) {
        // Generate JWT token
        JwtToken jwtToken = jwtService.generateJwtToken(user);

        // Generate UserResponse
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return AuthenticationResponse.builder()
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .user(userResponse)
                .build();
    }

    // Logout user
    public void logout(RefreshTokenRequest request) {
        // Get refresh token id from request
        var refreshToken = request.getRefreshToken();
        // Get refresh token id from refresh token string
        var refreshTokenId = jwtUtil.extractRefreshTokenId(refreshToken);
        // If refresh token id is valid and refresh token exists in database
        if (jwtUtil.isRefreshTokenValid(refreshToken) && refreshTokenRepository.existsById(refreshTokenId)) {
            // Delete refresh token from database
            refreshTokenRepository.deleteById(refreshTokenId);
        } else {
            throw new BadRequestException("Refresh token is invalid");
        }
    }

    // Logout all user from all devices
    public void logoutAll(RefreshTokenRequest request) {
        // Get refresh token id from request
        var refreshToken = request.getRefreshToken();
        // Get refresh token id from refresh token string
        var refreshTokenId = jwtUtil.extractRefreshTokenId(refreshToken);
        // If refresh token id is valid and refresh token exists in database
        if (jwtUtil.isRefreshTokenValid(refreshToken) && refreshTokenRepository.existsById(refreshTokenId)) {
            // Get user from user id in refresh token
            var user = userRepository.findUserByEmail(jwtUtil.extractUsernameRefreshToken(refreshToken))
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", jwtUtil.extractUsernameRefreshToken(refreshToken)));
            // Delete all refresh token from database
            refreshTokenRepository.deleteAllByUserId(user.getId());
        } else {
            throw new BadRequestException("Refresh token is invalid");
        }
    }

    // Refresh token
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        // Get refresh token id from request
        var refreshToken = request.getRefreshToken();
        // Get refresh token id from refresh token string
        var refreshTokenId = jwtUtil.extractRefreshTokenId(refreshToken);
        // If refresh token id is valid and refresh token exists in database
        if (jwtUtil.isRefreshTokenValid(refreshToken) && refreshTokenRepository.existsById(refreshTokenId)) {
            // Get user from user id in refresh token
            var user = userRepository.findUserByEmail(jwtUtil.extractUsernameRefreshToken(refreshToken))
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", jwtUtil.extractUsernameRefreshToken(refreshToken)));
            // Generate access token
            var accessToken = jwtUtil.generateAccessToken(user);

            // If refresh token rotation is enabled
            if (JWT_REFRESH_TOKEN_ROTATION) {
                // Delete refresh token from database
                refreshTokenRepository.deleteById(refreshTokenId);
                // Build refresh token object
                RefreshToken newRefreshToken = RefreshToken.builder()
                        .user(user)
                        .build();
                // Save refresh token to database
                refreshTokenRepository.save(newRefreshToken);
                // Generate extraClaims for refresh token
                Map<String, Object> extraClaims = Map.of(
                        "refreshTokenId", newRefreshToken.getId()
                );
                // Generate refresh token with extraClaims
                var newRefreshTokenString = jwtUtil.generateRefreshToken(extraClaims, user);

                return RefreshTokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(newRefreshTokenString)
                        .build();
            } else {
                return RefreshTokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
            }
        } else {
            throw new BadRequestException("Refresh token is invalid");
        }
    }

    // Confirm user email
    public SuccessResponse confirmToken(String token) {
        // Find confirmation token by token
        var confirmationToken = confirmationTokenService.findConfirmationTokenByToken(token)
                .orElseThrow(() -> new BadRequestException("Confirmation token not found"));
        // If confirmation token is already confirmed
        if (confirmationToken.getConfirmedAt() != null) {
            throw new BadRequestException("Email already confirmed");
        }
        // If confirmation token is expired
        if (confirmationToken.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new BadRequestException("Confirmation token expired");
        }
        // Set confirmation token confirmedAt
        confirmationTokenService.setConfirmedAt(token);
        // Enable user
        userService.enableUser(confirmationToken.getUser().getEmail());
        return SuccessResponse.builder()
                .message("Email confirmed")
                .build();
    }
}

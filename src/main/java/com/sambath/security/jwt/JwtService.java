package com.sambath.security.jwt;

import com.sambath.security.auth.dto.JwtToken;
import com.sambath.security.user.entity.RefreshToken;
import com.sambath.security.user.entity.User;
import com.sambath.security.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtToken generateJwtToken(User user) {
        // Generate JWT access token
        var accessToken = jwtUtil.generateAccessToken(user);

        // Build refresh token object
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .build();
        // Save refresh token to database
        refreshTokenRepository.save(refreshToken);

        // Generate extraClaims for refresh token
        Map<String, Object> extraClaims = Map.of(
                "refreshTokenId", refreshToken.getId()
        );

        // Generate refresh token with extraClaims
        var refreshTokenString = jwtUtil.generateRefreshToken(extraClaims, user);

        return JwtToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .build();
    }
}

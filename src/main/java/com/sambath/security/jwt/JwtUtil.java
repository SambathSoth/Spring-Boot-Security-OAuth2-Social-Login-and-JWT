package com.sambath.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {
    @Value("${app.auth.jwt.access-token-secret-key}")
    private String ACCESS_TOKEN_SECRET_KEY;
    @Value("${app.auth.jwt.refresh-token-secret-key}")
    private String REFRESH_TOKEN_SECRET_KEY;
    @Value("${app.auth.jwt.access-token-expiration-in-min}")
    private long ACCESS_TOKEN_EXPIRATION_IN_MIN;
    @Value("${app.auth.jwt.refresh-token-expiration-in-day}")
    private long REFRESH_TOKEN_EXPIRATION_IN_DAY;

    // Extract username from JWT access token
    public String extractUsernameAccessToken(String token) {
        return extractClaim(token, getAccessTokenSignInKey(), Claims::getSubject);
    }

    // Extract username from JWT refresh token
    public String extractUsernameRefreshToken(String token) {
        return extractClaim(token, getRefreshTokenSignInKey(), Claims::getSubject);
    }

    // Extract refresh token id from refresh token
    public String extractRefreshTokenId(String token) {
        return extractClaim(token, getRefreshTokenSignInKey(), Claims -> Claims.get("refreshTokenId", String.class));
    }

    // Extract claim from JWT token
    public <T> T extractClaim(String token, Key key, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, key);
        return claimsResolver.apply(claims);
    }

    // Generate JWT access token without extra claims
    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(new HashMap<>(), userDetails);
    }

    // Generate JWT access token with extra claims
    public String generateAccessToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date((System.currentTimeMillis())))
                .setExpiration(new Date((System.currentTimeMillis() + 1000 * 60 * ACCESS_TOKEN_EXPIRATION_IN_MIN)))
                .signWith(getAccessTokenSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Generate JWT refresh token with extra claims
    public String generateRefreshToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date((System.currentTimeMillis())))
                .setExpiration(new Date((
                        System.currentTimeMillis() + 1000 * 60 * 60 * 24 * REFRESH_TOKEN_EXPIRATION_IN_DAY
                )))
                .signWith(getRefreshTokenSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Check if JWT access token is valid
    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsernameAccessToken(token);
        return username.equals(userDetails.getUsername()) && !isAccessTokenExpired(token);
    }

    // Check if JWT refresh token is valid
    public boolean isRefreshTokenValid(String token) {
        return !isRefreshTokenExpired(token);
    }

    // Check if JWT access token is expired
    private boolean isAccessTokenExpired(String token) {
        return extractExpiration(token, getAccessTokenSignInKey()).before(new Date());
    }

    // Check if JWT refresh token is expired
    public boolean isRefreshTokenExpired(String token) {
        return extractExpiration(token, getRefreshTokenSignInKey()).before(new Date());
    }

    // Extract expiration date from JWT token
    private Date extractExpiration(String token, Key key) {
        return extractClaim(token, key, Claims::getExpiration);
    }

    // Extract all claims from JWT token
    private Claims extractAllClaims(String token, Key key) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Get access token sign in key from secret key
    private Key getAccessTokenSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(ACCESS_TOKEN_SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Get refresh token sign in key from secret key
    private Key getRefreshTokenSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(REFRESH_TOKEN_SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

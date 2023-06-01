package com.sambath.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // jwtUtil is a service that provides methods for generating and validating JWT tokens.
    private final JwtUtil jwtUtil;
    // UserDetailsService is an interface that loads user-specific data.
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization"); // Get authorization header
        final String jwt;
        final String userEmail;
        // If authorization header is null or does not start with "Bearer "
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Continue the filter chain
            return;
        }
        jwt = authorizationHeader.substring(7); // Get JWT
        userEmail = jwtUtil.extractUsernameAccessToken(jwt); // Extract user email from JWT token;
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load user details from database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            // If JWT token is valid
            if (jwtUtil.isAccessTokenValid(jwt, userDetails)) {
                // Create authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                // Set authentication token details
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // Update SecurityContext using authentication token
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response); // Continue the filter chain
    }
}

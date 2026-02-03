package com.denari.manager.security;

import com.denari.manager.models.entity.User.User;
import com.denari.manager.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // List of paths that don't require JWT authentication
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/auth/send-otp",
            "/api/auth/verify-otp",
            "/api/webhooks/modern-treasury",
            "/actuator/health",
            "/error"
    );

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Skip JWT processing for excluded paths
        if (EXCLUDED_PATHS.stream().anyMatch(requestPath::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtUtil.getTokenFromRequest(request);
        if (token != null && jwtUtil.validateToken(token)) {
            try {
                // ✅ PRODUCTION: Extract userId from JWT instead of email
                Long userId = jwtUtil.extractUserId(token);

                // ✅ PRODUCTION: Load user by ID (not email)
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

                // Create UserDetails with current user data
                UserDetails userDetails = org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail()) // Use current email from database
                        .password("{noop}")
                        .authorities("ROLE_USER")
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .disabled(user.getStatus() != User.UserStatus.ACTIVE && user.getStatus() != User.UserStatus.PENDING)
                        .build();

                SecurityContextHolder.getContext().setAuthentication(
                        new JwtAuthenticationToken(userDetails, token, userDetails.getAuthorities())
                );

            } catch (Exception e) {
                log.error("JWT authentication failed: {}", e.getMessage());
                // Don't set authentication - request will be rejected
            }
        }

        filterChain.doFilter(request, response);
    }
}



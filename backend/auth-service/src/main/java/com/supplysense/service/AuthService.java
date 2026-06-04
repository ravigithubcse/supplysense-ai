package com.supplysense.service;

import com.supplysense.dto.AuthDtos.*;
import com.supplysense.model.Organization;
import com.supplysense.model.Role;
import com.supplysense.model.User;
import com.supplysense.repository.OrganizationRepository;
import com.supplysense.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Organization organization = null;
        if (request.getOrganizationName() != null && !request.getOrganizationName().isBlank()) {
            organization = organizationRepository.save(
                    Organization.builder()
                            .name(request.getOrganizationName())
                            .slug(generateSlug(request.getOrganizationName()))
                            .build()
            );
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.USER))
                .organization(organization)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (user.isAccountLocked()) {
            throw new LockedException("Account is locked. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid credentials");
        }

        // Reset failed attempts on success
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        try {
            Claims claims = jwtService.validateToken(request.getRefreshToken());
            String userId = claims.getSubject();
            String tokenType = claims.get("type", String.class);

            if (!"refresh".equals(tokenType)) {
                throw new JwtException("Invalid token type");
            }

            User user = userRepository.findById(java.util.UUID.fromString(userId))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Revoke old refresh token
            jwtService.blacklistToken(request.getRefreshToken());

            return buildAuthResponse(user);
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
    }

    public void logout(String accessToken) {
        jwtService.blacklistToken(accessToken);
        log.debug("Token blacklisted on logout");
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            Claims claims = jwtService.validateToken(token);
            Set<String> roles = Set.of(
                    claims.get("roles", String.class).split(",")
            );

            return TokenValidationResponse.builder()
                    .valid(true)
                    .userId(claims.getSubject())
                    .roles(roles)
                    .organizationId(claims.get("orgId", String.class))
                    .expiresAt(claims.getExpiration().getTime())
                    .build();
        } catch (JwtException e) {
            return TokenValidationResponse.builder().valid(false).build();
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(user.getRoles())
                .organizationId(user.getOrganization() != null
                        ? user.getOrganization().getId().toString() : null)
                .organizationName(user.getOrganization() != null
                        ? user.getOrganization().getName() : null)
                .lastLoginAt(user.getLastLoginAt())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration() / 1000)
                .user(userInfo)
                .build();
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLocked(true);
            log.warn("Account locked due to too many failed attempts: {}", user.getEmail());
        }

        userRepository.save(user);
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}

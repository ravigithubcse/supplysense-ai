package com.supplysense.service;

import com.supplysense.model.Role;
import com.supplysense.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:900000}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    private final StringRedisTemplate redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "token:refresh:";

    public String generateAccessToken(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toSet());

        String orgId = user.getOrganization() != null
                ? user.getOrganization().getId().toString()
                : null;

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("roles", String.join(",", roles))
                .claim("orgId", orgId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .id(UUID.randomUUID().toString())
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        String tokenId = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", "refresh")
                .claim("tokenId", tokenId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();

        // Store refresh token in Redis
        String key = REFRESH_TOKEN_PREFIX + user.getId() + ":" + tokenId;
        redisTemplate.opsForValue().set(key, token, Duration.ofMillis(refreshExpiration));

        return token;
    }

    public Claims validateToken(String token) throws JwtException {
        // Check blacklist
        String jti = extractJti(token);
        if (jti != null && isTokenBlacklisted(jti)) {
            throw new JwtException("Token has been revoked");
        }

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public void blacklistToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String jti = claims.getId();
            Instant expiry = claims.getExpiration().toInstant();
            Duration ttl = Duration.between(Instant.now(), expiry);

            if (!ttl.isNegative() && jti != null) {
                redisTemplate.opsForValue().set(
                        TOKEN_BLACKLIST_PREFIX + jti,
                        "revoked",
                        ttl
                );
            }
        } catch (JwtException e) {
            log.debug("Could not blacklist expired/invalid token: {}", e.getMessage());
        }
    }

    public boolean isTokenBlacklisted(String jti) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + jti)
        );
    }

    private String extractJti(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getId();
        } catch (Exception e) {
            return null;
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }
}

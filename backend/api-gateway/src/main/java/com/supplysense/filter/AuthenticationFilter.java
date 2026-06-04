package com.supplysense.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/actuator"
    );

    @Value("${jwt.secret}")
    private String jwtSecret;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            String correlationId = UUID.randomUUID().toString();

            // Add correlation ID to all requests
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Correlation-ID", correlationId)
                    .build();

            // Skip authentication for public endpoints
            if (isPublicEndpoint(path)) {
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }

            // Extract and validate JWT
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", path);
                return unauthorizedResponse(exchange, "Missing authorization token");
            }

            String token = authHeader.substring(7);
            try {
                Claims claims = validateToken(token);
                String userId = claims.getSubject();
                String roles = claims.get("roles", String.class);
                String orgId = claims.get("orgId", String.class);

                ServerHttpRequest enrichedRequest = modifiedRequest.mutate()
                        .header("X-User-ID", userId)
                        .header("X-User-Roles", roles != null ? roles : "")
                        .header("X-Org-ID", orgId != null ? orgId : "")
                        .build();

                log.debug("Authenticated request: userId={}, path={}, correlationId={}", userId, path, correlationId);
                return chain.filter(exchange.mutate().request(enrichedRequest).build());

            } catch (JwtException e) {
                log.warn("Invalid JWT token for path: {}, error: {}", path, e.getMessage());
                return unauthorizedResponse(exchange, "Invalid or expired token");
            }
        };
    }

    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        var responseBody = String.format("{\"error\": \"%s\", \"status\": 401}", message);
        var buffer = exchange.getResponse().bufferFactory()
                .wrap(responseBody.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}

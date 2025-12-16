package com.socialmedia.gateway.filter;

import com.socialmedia.gateway.security.JwtTokenProvider;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT authentication filter for API Gateway.
 * Validates JWT tokens and adds user context to request headers.
 */
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtTokenProvider jwtTokenProvider;

    // Paths that don't require authentication
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/validate"
    );

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            // Skip authentication for excluded paths
            if (isExcludedPath(path)) {
                return chain.filter(exchange);
            }

            // Extract JWT token from Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            // Validate token
            if (!jwtTokenProvider.validateToken(token)) {
                return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
            }

            // Extract user information from token
            try {
                String userId = jwtTokenProvider.getUserId(token);
                String email = jwtTokenProvider.getEmail(token);
                List<String> roles = jwtTokenProvider.getRoles(token);

                // Add user context to request headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Email", email)
                        .header("X-User-Roles", String.join(",", roles))
                        .build();

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .build();

                return chain.filter(modifiedExchange);
            } catch (Exception e) {
                return onError(exchange, "Failed to extract user information from token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * Check if path is excluded from authentication.
     */
    private boolean isExcludedPath(String path) {
        // Remove trailing spaces and normalize path
        String normalizedPath = path.trim();
        return EXCLUDED_PATHS.stream().anyMatch(normalizedPath::startsWith);
    }

    /**
     * Handle authentication errors.
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        String errorResponse = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                java.time.Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath()
        );
        
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes()))
        );
    }

    public static class Config {
        // Configuration properties if needed
    }
}

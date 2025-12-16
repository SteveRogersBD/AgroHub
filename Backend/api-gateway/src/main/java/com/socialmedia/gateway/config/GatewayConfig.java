package com.socialmedia.gateway.config;

import com.socialmedia.gateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for API Gateway routing.
 * Defines routes for all backend microservices.
 */
@Configuration
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${services.auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${services.user-service.url:http://localhost:8082}")
    private String userServiceUrl;

    @Value("${services.follow-service.url:http://localhost:8083}")
    private String followServiceUrl;

    @Value("${services.post-service.url:http://localhost:8084}")
    private String postServiceUrl;

    @Value("${services.comment-service.url:http://localhost:8085}")
    private String commentServiceUrl;

    @Value("${services.like-service.url:http://localhost:8086}")
    private String likeServiceUrl;

    @Value("${services.feed-service.url:http://localhost:8087}")
    private String feedServiceUrl;

    @Value("${services.notification-service.url:http://localhost:8088}")
    private String notificationServiceUrl;

    public GatewayConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configure routes for all microservices.
     * Routes requests based on path patterns to appropriate backend services.
     * Applies JWT authentication filter to all routes.
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service routes
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(authServiceUrl))
                
                // User Service routes
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(userServiceUrl))
                
                // Follow Service routes
                .route("follow-service", r -> r
                        .path("/api/follows/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(followServiceUrl))
                
                // Post Service routes
                .route("post-service", r -> r
                        .path("/api/posts/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(postServiceUrl))
                
                // Comment Service routes
                .route("comment-service", r -> r
                        .path("/api/comments/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(commentServiceUrl))
                
                // Like Service routes
                .route("like-service", r -> r
                        .path("/api/likes/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(likeServiceUrl))
                
                // Feed Service routes
                .route("feed-service", r -> r
                        .path("/api/feed/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(feedServiceUrl))
                
                // Notification Service routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri(notificationServiceUrl))
                
                .build();
    }
}

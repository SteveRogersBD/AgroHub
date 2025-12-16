package com.socialmedia.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for API Gateway.
 * Allows cross-origin requests from specified origins.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allow all origins (configure specific origins in production)
        corsConfig.setAllowedOriginPatterns(List.of("*"));
        
        // Allow common HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Allow all headers
        corsConfig.setAllowedHeaders(List.of("*"));
        
        // Expose Authorization header
        corsConfig.setExposedHeaders(List.of("Authorization"));
        
        // Allow credentials
        corsConfig.setAllowCredentials(true);
        
        // Max age for preflight requests
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}

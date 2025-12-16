package com.socialmedia.feed.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI feedServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Feed Service API")
                        .description("Feed generation service for social media platform. " +
                                "Aggregates posts from followed users with enriched metadata (likes, comments).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Social Media Platform Team")
                                .email("support@socialmedia.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8087")
                                .description("Development server"),
                        new Server()
                                .url("http://localhost:8080/api/feed")
                                .description("API Gateway")
                ));
    }
}

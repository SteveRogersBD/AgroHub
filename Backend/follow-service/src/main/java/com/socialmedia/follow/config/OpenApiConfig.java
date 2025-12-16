package com.socialmedia.follow.config;

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
    public OpenAPI followServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Follow Service API")
                        .description("Follow relationship management service for social media platform. " +
                                "Handles follower/following relationships, lists, and statistics.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Social Media Platform Team")
                                .email("support@socialmedia.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8083")
                                .description("Development server"),
                        new Server()
                                .url("http://localhost:8080/api/follows")
                                .description("API Gateway")
                ));
    }
}

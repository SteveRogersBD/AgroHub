package com.socialmedia.user.config;

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
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("User profile management service for social media platform. " +
                                "Handles user profile CRUD operations and user search functionality.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Social Media Platform Team")
                                .email("support@socialmedia.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Development server"),
                        new Server()
                                .url("http://localhost:8080/api/users")
                                .description("API Gateway")
                ));
    }
}

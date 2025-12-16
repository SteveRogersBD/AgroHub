package com.socialmedia.post.config;

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
    public OpenAPI postServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Post Service API")
                        .description("Post management service for social media platform. " +
                                "Handles post creation, updates, deletion (soft delete), and retrieval.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Social Media Platform Team")
                                .email("support@socialmedia.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8084")
                                .description("Development server"),
                        new Server()
                                .url("http://localhost:8080/api/posts")
                                .description("API Gateway")
                ));
    }
}

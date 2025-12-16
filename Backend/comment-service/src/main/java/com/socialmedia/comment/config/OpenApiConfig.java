package com.socialmedia.comment.config;

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
    public OpenAPI commentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Comment Service API")
                        .description("Comment management service for social media platform. " +
                                "Handles comment creation, updates, deletion, and retrieval for posts.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Social Media Platform Team")
                                .email("support@socialmedia.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8085")
                                .description("Development server"),
                        new Server()
                                .url("http://localhost:8080/api/comments")
                                .description("API Gateway")
                ));
    }
}

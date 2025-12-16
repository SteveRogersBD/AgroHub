package com.socialmedia.like.config;

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
    public OpenAPI likeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Like Service API")
                        .description("Like management service for social media platform. " +
                                "Handles like/unlike operations, like counts, and batch operations.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Social Media Platform Team")
                                .email("support@socialmedia.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8086")
                                .description("Development server"),
                        new Server()
                                .url("http://localhost:8080/api/likes")
                                .description("API Gateway")
                ));
    }
}

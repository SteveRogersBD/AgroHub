package com.socialmedia.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for API Gateway.
 * Provides consistent error response format across all services.
 */
@Component
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = determineHttpStatus(ex);
        String message = determineErrorMessage(ex);
        String errorCode = determineErrorCode(ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("path", exchange.getRequest().getPath().value());

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    /**
     * Determine HTTP status code based on exception type.
     */
    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            return HttpStatus.valueOf(((ResponseStatusException) ex).getStatusCode().value());
        } else if (ex instanceof org.springframework.web.server.ServerWebInputException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Determine error message based on exception.
     */
    private String determineErrorMessage(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            return ((ResponseStatusException) ex).getReason();
        } else if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
            return "Service temporarily unavailable";
        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            return "Request timeout - service took too long to respond";
        }
        return ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";
    }

    /**
     * Determine error code based on exception type.
     */
    private String determineErrorCode(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            HttpStatus status = HttpStatus.valueOf(((ResponseStatusException) ex).getStatusCode().value());
            if (status == HttpStatus.UNAUTHORIZED) {
                return "AUTHENTICATION_ERROR";
            } else if (status == HttpStatus.FORBIDDEN) {
                return "AUTHORIZATION_ERROR";
            } else if (status == HttpStatus.NOT_FOUND) {
                return "RESOURCE_NOT_FOUND";
            } else if (status == HttpStatus.BAD_REQUEST) {
                return "VALIDATION_ERROR";
            }
        } else if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
            return "SERVICE_UNAVAILABLE";
        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            return "GATEWAY_TIMEOUT";
        }
        return "INTERNAL_SERVER_ERROR";
    }
}

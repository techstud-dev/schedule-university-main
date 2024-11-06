package com.techstud.scheduleuniversity.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.systemName}")
    private String systemName;

    @ExceptionHandler(Exception.class)
    public Mono<ServerResponse> handleException(Exception exception, HttpRequest request) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("systemName", systemName);
        response.put("serviceName", applicationName);
        response.put("callId", Objects.requireNonNull(request.getHeaders().get("callId")).get(0));
        response.put("message", exception.getMessage());
        log.error(exception.getMessage(), exception);
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(response);
    }
}

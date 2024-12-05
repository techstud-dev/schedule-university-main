package com.techstud.scheduleuniversity.handler;

import com.techstud.scheduleuniversity.exception.ParserException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.DeserializationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${spring.application.name}")
    private String systemName;

    @Value("${spring.application.systemName}")
    private String applicationName;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("systemName", systemName);
        response.put("applicationName", applicationName);
        response.put("error", e.getMessage());
        log.error("Unhandled exception", e);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {ResponseStatusException.class, DeserializationException.class})
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException e) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("systemName", systemName);
        response.put("applicationName", applicationName);
        response.put("error", e.getReason());
        log.error("Request exception handle", e);
        return new ResponseEntity<>(response, e.getStatusCode());
    }

    @ExceptionHandler({MalformedJwtException.class, JwtException.class})
    public ResponseEntity<Map<String, String>> handleMalformedJwtException(MalformedJwtException e) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("systemName", systemName);
        response.put("applicationName", applicationName);
        response.put("error", e.getMessage());
        log.error("Auth handle", e);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String,String>> handleAuthorizationDeniedException(AuthorizationDeniedException e, HttpServletRequest request) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("systemName", systemName);
        response.put("applicationName", applicationName);
        response.put("error", e.getMessage());
        log.error("AuthorizationDeniedException handle: {}, remoteAddress: {}, authResult: {}",
                e.getMessage(),
                request.getRemoteAddr(),
                e.getAuthorizationResult(), e);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ParserException.class)
    public ResponseEntity<Map<String, String>> handleParserException(ParserException e) {
        Map<String, String> response = e.getParserResponse();
        log.error("Error import schedule from university", e);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}

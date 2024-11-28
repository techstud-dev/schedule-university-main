package com.techstud.scheduleuniversity.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

@Slf4j
public class HttpAccessLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {


        logRequest(request);

        filterChain.doFilter(request, response);

        logResponse(response);
    }

    private void logRequest(HttpServletRequest request) {
        StringBuilder message = new StringBuilder();

        message.append("HTTP Method: ").append(request.getMethod()).append("\n");
        message.append("Request URL: ").append(request.getRequestURL().toString()).append("\n");

        message.append("Headers:\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            // Маскируем заголовок Authorization
            if ("Authorization".equalsIgnoreCase(headerName)) {
                headerValue = maskAuthorization(headerValue);
            }

            message.append("  ").append(headerName).append(": ").append(headerValue).append("\n");
        }

        log.info("HTTP Request:\n{}", message);
    }


    private void logResponse(HttpServletResponse response) {
        StringBuilder message = new StringBuilder();

        message.append("Response Status: ").append(response.getStatus()).append("\n");

        message.append("Response Headers:\n");
        response.getHeaderNames().forEach(headerName -> {
            String headerValue = response.getHeader(headerName);
            message.append("  ").append(headerName).append(": ").append(headerValue).append("\n");
        });

        log.info("HTTP Response:\n{}", message);
    }

    private String maskAuthorization(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.length() <= 10) {
            return "********";
        }
        int startLength = 7;
        int endLength = 3;

        String start = authorizationHeader.substring(0, startLength);
        String end = authorizationHeader.substring(authorizationHeader.length() - endLength);
        return start + "***" + end;
    }

}

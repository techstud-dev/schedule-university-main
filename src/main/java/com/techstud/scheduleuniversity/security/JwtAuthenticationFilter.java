package com.techstud.scheduleuniversity.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.util.BsonUtils.toJson;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${spring.application.name}")
    private String systemName;

    @Value("${spring.application.systemName}")
    private String applicationName;

    private final SecretKey jwtSecretKey;
    private final String expectedIssuer;
    private final String expectedAudience;

    public JwtAuthenticationFilter(String secret, String expectedIssuer, String expectedAudience) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expectedIssuer = expectedIssuer;
        this.expectedAudience = expectedAudience;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/v3/api-docs") ||
                request.getRequestURI().startsWith("/swagger-ui") ||
                request.getRequestURI().startsWith("/swagger-ui.html")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                JwtParser jwtParser = Jwts.parser()
                        .requireIssuer(expectedIssuer)
                        .requireAudience(expectedAudience)
                        .verifyWith(jwtSecretKey)
                        .build();

                Jwt<?, ?> parsedJwt = jwtParser.parse(jwt);

                if (parsedJwt.getPayload() instanceof Claims claims) {
                    String username = claims.getSubject();

                    List<String> roles = claims.get("roles", List.class);

                    List<? extends GrantedAuthority> authorites = Collections.emptyList();

                    // Убедиться, что токен выдан нашим сервисом авторизации
                    String issuer = claims.getIssuer();
                    if (issuer == null || !issuer.equals(expectedIssuer)) {
                        throw new JwtException("Invalid issuer");
                    }

                    //Убедиться, что токен предназначен для нашего сервиса
                    Set<String> audience = claims.getAudience();
                    if (audience == null || !audience.contains(expectedAudience)) {
                        throw new JwtException("Invalid audience");
                    }

                    //Убедиться, что время действия токена не истекло
                    Date expiration = claims.getExpiration();
                    if (expiration == null || expiration.before(new Date())) {
                        throw new JwtException("Token has expired");
                    }

                    if (roles != null) {
                        authorites = roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList());
                    }

                    User principal = new User(username, "", authorites);

                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            authorites
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (MalformedJwtException ex) {
            handleJwtError(request, response, "Invalid JWT token", ex);
            return;
        } catch (ExpiredJwtException ex) {
            handleJwtError(request, response, "Expired JWT token", ex);
            return;
        } catch (UnsupportedJwtException ex) {
            handleJwtError(request, response, "Unsupported JWT token",ex);
            return;
        } catch (IllegalArgumentException ex) {
            handleJwtError(request, response, "JWT claims string is empty", ex);
            return;
        } catch (SignatureException ex) {
            handleJwtError(request, response, "Invalid JWT signature", ex);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void handleJwtError(HttpServletRequest request,
                                HttpServletResponse response,
                                String errorMessage, Exception e) throws IOException {
        log.error("Authentication error: {}, remoteAddress: {}", e.getMessage(), request.getRemoteAddr());

        Map<String, String> errorResponse = new LinkedHashMap<>();
        errorResponse.put("systemName", systemName);
        errorResponse.put("applicationName", applicationName);
        errorResponse.put("error", errorMessage);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(toJson(errorResponse));
    }

    private String toJson(Map<String, String> map) {
        StringBuilder jsonBuilder = new StringBuilder("{");
        map.forEach((key, value) -> jsonBuilder
                .append("\"")
                .append(key)
                .append("\":\"")
                .append(value)
                .append("\","));
        jsonBuilder.deleteCharAt(jsonBuilder.length() - 1).append("}");
        return jsonBuilder.toString();
    }
}

package com.techstud.scheduleuniversity.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

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
        } catch (JwtException ex) {
            log.error("JWT has been invalid", ex);
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
}

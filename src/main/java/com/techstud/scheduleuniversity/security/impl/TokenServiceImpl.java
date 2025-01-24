package com.techstud.scheduleuniversity.security.impl;

import com.techstud.scheduleuniversity.security.TokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class TokenServiceImpl implements TokenService {

    @Value("${jwt.secret}")
    private String stringSecret;

    @Value("${jwt.issuer}")
    private String issuer;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(stringSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateServiceToken() {
        return Jwts.builder()
                .issuer(issuer)
                .claim("type", "jwt")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}

package com.techstud.scheduleuniversity.security.impl;

import com.techstud.scheduleuniversity.security.TokenService;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class TokenServiceImpl implements TokenService {

    @Value("${jwt.secret.key}")
    private SecretKey SECRET_KEY;

    @Value("${jwt.audience}")
    private String issuer;

    @Override
    public String generateServiceToken() {
        return Jwts.builder()
                .issuer(issuer)
                .claim("type", "jwt")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(SECRET_KEY)
                .compact();
    }
}

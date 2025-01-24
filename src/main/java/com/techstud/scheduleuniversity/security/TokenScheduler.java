package com.techstud.scheduleuniversity.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenScheduler {

    private final AuthClient authClient;

    @PostConstruct
    public void init() {
        log.info("Initializing token scheduler...");
        authClient.authenticateService();
    }

    @Scheduled(fixedRateString = "PT1M")
    public void refreshAccessToken() {
        log.info("Refreshing access token at {}", Instant.now());
        authClient.refreshTokens();
    }

    @Scheduled(fixedRateString = "PT2M")
    public void refreshRefreshToken() {
        log.info("Re-authenticating service at {}", Instant.now());
        authClient.authenticateService();
    }
}

package com.techstud.scheduleuniversity.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    @Async
    @Scheduled(fixedRateString = "PT14M")
    public void refreshAccessToken() {
        log.info("Refreshing access token...");
        authClient.refreshTokens();
    }

    @Async
    @Scheduled(fixedRateString = "PT1H59M")
    public void refreshRefreshToken() {
        log.info("Refreshing refresh token...");
        authClient.authenticateService();
    }
}

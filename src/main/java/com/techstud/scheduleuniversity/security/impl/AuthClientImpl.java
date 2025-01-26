package com.techstud.scheduleuniversity.security.impl;

import com.techstud.scheduleuniversity.security.AuthClient;
import com.techstud.scheduleuniversity.security.TokenManager;
import com.techstud.scheduleuniversity.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthClientImpl implements AuthClient {

    @Value("${auth.service.url}")
    private String baseUrl;

    private final CloseableHttpClient httpClient;
    private final TokenManager tokenManager;
    private final TokenService jwtGenerateService;

    @Override
    public void authenticateService() {
        String jwtToken = jwtGenerateService.generateServiceToken();
        String validateUrl = baseUrl + "/service/auth/validate-service";

        executeWithRetry(() -> {
            HttpPost httpPost = new HttpPost(validateUrl);
            httpPost.setHeader("Authorization", "Bearer " + jwtToken);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                handleHttpAuthResponse(response);
            } catch (IOException e) {
                log.error("Error during service authentication: {}", e.getMessage());
                throw new RuntimeException("Failed to authenticate service", e);
            }
        });
    }

    @Override
    public void refreshTokens() {
        String currentRefreshToken = String.valueOf(tokenManager.getRefreshToken());
        String refreshUrl = baseUrl + "/service/auth/refresh-token";

        if (currentRefreshToken == null || currentRefreshToken.isEmpty()) {
            log.warn("Refresh token is null or empty. Re-authenticating...");
            authenticateService();
            return;
        }

        executeWithRetry(() -> {
            HttpPost httpPost = new HttpPost(refreshUrl);
            httpPost.setHeader("Authorization", "Bearer " + currentRefreshToken);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                handleHttpRefreshResponse(response);
            } catch (IOException e) {
                log.error("Error during token refresh: {}", e.getMessage());
                throw new RuntimeException("Failed to refresh tokens", e);
            }
        });
    }

    private void handleHttpAuthResponse(CloseableHttpResponse response) {
        int statusCode = response.getCode();

        if (statusCode == 200) {
            Header accessTokenHeader = response.getFirstHeader("Access-Token");
            Header refreshTokenHeader = response.getFirstHeader("Refresh-Token");

            if (accessTokenHeader == null || refreshTokenHeader == null) {
                throw new RuntimeException("Missing required headers in response");
            }

            String accessToken = accessTokenHeader.getValue();
            String refreshToken = refreshTokenHeader.getValue();

            tokenManager.updateTokens(accessToken, refreshToken);
            log.info("Service authenticated successfully.");
        }else {
            throw new RuntimeException("Authentication failed with status code: " + statusCode);
        }
    }

    private void handleHttpRefreshResponse(CloseableHttpResponse response) {
        int statusCode = response.getCode();

        if (statusCode == 200) {
            Header accessTokenHeader = response.getFirstHeader("Access-Token");

            if (accessTokenHeader == null) {
                throw new RuntimeException("Missing required headers in response");
            }

            String accessToken = accessTokenHeader.getValue();

            tokenManager.updateAccessToken(accessToken);
            log.info("Access token refreshed successfully.");
        } else {
            throw new RuntimeException("Authentication failed with status code: " + statusCode);
        }
    }

    private void executeWithRetry(Runnable action) {
        for (int i = 0; i < 7; i++) {
            try {
                action.run();
                return;
            } catch (Exception e) {
                log.warn("Attempt {} failed: {}", i + 1, e.getMessage());
                if (i < 7 - 1) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
        throw new RuntimeException("Max retries exceeded.");
    }
}

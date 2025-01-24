package com.techstud.scheduleuniversity.security;

public interface AuthClient {

    void authenticateService();
    void refreshTokens();

}

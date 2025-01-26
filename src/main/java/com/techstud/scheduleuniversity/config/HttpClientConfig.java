package com.techstud.scheduleuniversity.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Value("${http.client.connection-timeout}")
    private String connectionTimeOut;

    @Value("${http.client.socket-timeout}")
    private String socketTimeOut;

    @Value("${http.client.request-timeout}")
    private String requestTimeout;

    @Value("${http.client.proxy.enabled}")
    private boolean proxyEnabled;

    @Value("${http.client.proxy.host}")
    private String proxyHost;

    @Value("${http.client.proxy.port}")
    private String proxyPort;

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(Integer.parseInt(connectionTimeOut)))
                .setResponseTimeout(Timeout.ofMilliseconds(Integer.parseInt(socketTimeOut)))
                .build();

        if (proxyEnabled) {
            HttpHost httpProxyHost = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
            return HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .setProxy(httpProxyHost)
                    .build();
        } else {
            return HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .build();
        }

    }
}

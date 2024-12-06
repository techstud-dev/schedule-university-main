package com.techstud.scheduleuniversity.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.TextUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FetcherHttpUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> HttpClientResponseHandler<T> createResponseHandler(Class<T> responseType, boolean isHtml) {
        return response -> {
            if (response.getCode() != 200) {
                log.error("Error while getting response, status code: {}", response.getCode());
                throw new IOException("Unexpected response code: " + response.getCode());
            }

            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (TextUtils.isBlank(responseBody)) {
                log.error("Response body is empty: {}", responseBody);
                throw new IOException("Empty or null response body: " + responseBody);
            }

            if (isHtml) {
                @SuppressWarnings("unchecked")
                T result = (T) responseBody;
                return result;
            } else {
                return MAPPER.readValue(responseBody, responseType);
            }
        };
    }
}

package com.techstud.scheduleuniversity.service.impl.fetcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstud.scheduleuniversity.dto.fetcher.GroupData;
import com.techstud.scheduleuniversity.service.GroupFetcherService;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TltsuGroupDataFetchService implements GroupFetcherService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<GroupData> fetchGroupsData() {
        String institutesUrl = "https://its.tltsu.ru/api/institutes";
        List<GroupData> groupDataList = new ArrayList<>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(institutesUrl);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                System.out.println(responseBody);
                Map<Long, String> jsonMap = new HashMap<>();

            } catch (Exception e) {
                log.error(e.getMessage());
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return groupDataList;
    }
}

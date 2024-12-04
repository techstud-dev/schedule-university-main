package com.techstud.scheduleuniversity.service.impl.fetcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstud.scheduleuniversity.dto.fetcher.GroupData;
import com.techstud.scheduleuniversity.service.GroupFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MephiGroupDataFetchService implements GroupFetcherService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://home.mephi.ru";
        int[] levels = {0, 1, 2, 3, 4}; // Бакалавр, специалитет, магистратура и т.д.

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            String csrfToken = fetchCsrfToken(httpClient, baseUrl);
            if (csrfToken == null) {
                log.error("Failed to fetch CSRF token");
                return groupDataList;
            }

            for (int level : levels) {
                String levelUrl = baseUrl + "/study_groups?level=" + level + "&organization_id=1&term_id=19";

                HttpGet httpGet = getHttpGet(levelUrl, csrfToken);

                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                    Document document = Jsoup.parse(responseBody);

                    Elements groups = document.select("a.list-group-item");

                    for (Element groupElement : groups) {
                        String groupCode = groupElement.text();

                        String href = groupElement.attr("href");
                        String universityGroupId = href.split("/")[2];

                        groupDataList.add(new GroupData(groupCode, universityGroupId));
                    }

                    if (groups.isEmpty()) {
                        log.warn("No groups found for level {}", level);
                    }

                    groupDataList = groupDataList.stream()
                            .sorted(Comparator.comparing(GroupData::groupCode))
                            .collect(Collectors.toList());

                } catch (Exception e) {
                    log.error("Error processing level {}: {}", level, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error fetching group data from MEPHI", e);
        }

        return  groupDataList;
    }

    private String fetchCsrfToken(CloseableHttpClient httpClient, String baseUrl) {
        String csrfToken = null;
        String mainUrl = baseUrl + "/study_groups?level=0&organization_id=1&term_id=19";

        try {
            HttpGet httpGet = new HttpGet(mainUrl);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                Document document = Jsoup.parse(responseBody);
                csrfToken = document.select("meta[name=csrf-token]").attr("content");
            }
        } catch (Exception e) {
            log.error("Error fetching CSRF token: {}", e.getMessage());
        }

        return csrfToken;
    }

    private static HttpGet getHttpGet(String url, String csrfToken) {
        HttpGet httpGet = new HttpGet(url);

        // можно куки не передавать
        Header[] commonHeaders = {
                new BasicHeader("Accept", "application/json, text/javascript, */*; q=0.01"),
                new BasicHeader("Accept-Encoding", "gzip, deflate, br, zstd"),
                new BasicHeader("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7"),
                new BasicHeader("Connection", "keep-alive"),
                new BasicHeader("Content-Type", "application/json; charset=UTF-8"),
                new BasicHeader("Host", "home.mephi.ru"),
                new BasicHeader("Origin", "https://home.mephi.ru"),
                new BasicHeader("Referer", "https://home.mephi.ru/study_groups?level=0&organization_id=1&term_id=19"),
                new BasicHeader("Sec-CH-UA", "\"Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\""),
                new BasicHeader("Sec-CH-UA-Mobile", "?0"),
                new BasicHeader("Sec-CH-UA-Platform", "\"Windows\""),
                new BasicHeader("Sec-Fetch-Dest", "empty"),
                new BasicHeader("Sec-Fetch-Mode", "cors"),
                new BasicHeader("Sec-Fetch-Site", "same-origin"),
                new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"),
                new BasicHeader("X-CSRF-Token", csrfToken),
                new BasicHeader("X-Requested-With", "XMLHttpRequest")
        };

        httpGet.setHeaders(commonHeaders);
        return httpGet;
    }
}

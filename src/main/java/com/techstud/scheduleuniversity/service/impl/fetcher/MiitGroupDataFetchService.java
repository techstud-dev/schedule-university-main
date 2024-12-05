package com.techstud.scheduleuniversity.service.impl.fetcher;

import com.techstud.scheduleuniversity.dto.fetcher.GroupData;
import com.techstud.scheduleuniversity.service.GroupFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MiitGroupDataFetchService implements GroupFetcherService {

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://www.miit.ru/timetable";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpGet httpGet = new HttpGet(baseUrl);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                Document document = Jsoup.parse(responseBody);

                Elements groupElements = document.select("div.dropdown-menu .dropdown-item");
                for (Element groupElement : groupElements) {
                    String href = groupElement.attr("href").trim();
                    String groupCode = groupElement.text().trim();

                    if (href.isEmpty() || href.equals("/") || href.contains(".pdf") || groupCode.isEmpty()) {
                        System.out.println("Skipping invalid href or groupCode: href=" + href + ", groupCode= " + groupCode);
                        continue;
                    }

                    String universityGroupId = href.startsWith("/timetable/") ? href.replace("/timetable/", "") : href;

                    if (!isNumeric(universityGroupId)) {
                        System.out.println("Skipping non-numeric universityGroupId: " + universityGroupId);
                        continue;
                    }

                    groupDataList.add(new GroupData(groupCode, universityGroupId));
                }

            } catch (Exception e) {
                log.error("Error processing pattern {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error fetching group data from MIIT", e);
        }

        return  groupDataList.stream()
                .sorted(Comparator.comparing(GroupData::universityGroupId))
                .collect(Collectors.toList());
    }

    private boolean isNumeric(String str) {
        return str.matches("\\d+");
    }
}
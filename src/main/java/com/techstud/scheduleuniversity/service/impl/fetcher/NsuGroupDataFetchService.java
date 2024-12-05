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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NsuGroupDataFetchService implements GroupFetcherService {

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://table.nsu.ru/faculties";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                Document document = Jsoup.parse(responseBody);

                Elements faculties = document.select("a.faculty");
                for (Element faculty : faculties) {
                    String facultyLink = faculty.attr("href");
                    if (!facultyLink.isEmpty()) {
                        parseFacultyGroups(httpClient, facultyLink, groupDataList);
                    }
                }

            } catch (Exception e) {
                log.error("Error processing pattern {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error fetching group data from NSU", e);
        }

        return  groupDataList.stream()
                .sorted(Comparator.comparing(GroupData::universityGroupId))
                .collect(Collectors.toList());
    }

    private void parseFacultyGroups(CloseableHttpClient httpClient, String facultyLink, List<GroupData> groupDataList) {
        String facultyUrl = "https://table.nsu.ru" + facultyLink;

        try {
            HttpGet httpGet = new HttpGet(facultyUrl);

            try (CloseableHttpResponse facultyResponse = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(facultyResponse.getEntity(), StandardCharsets.UTF_8);
                Document document = Jsoup.parse(responseBody);

                Elements groups = document.select("a.group");
                for (Element group : groups) {
                    String groupCode = group.text().trim();
                    String groupHref = group.attr("href").trim();

                    if (!groupCode.isEmpty() && groupHref.startsWith("/group/")) {
                        String universityGroupId = groupHref.replace("/group/", "");
                        groupDataList.add(new GroupData(groupCode, universityGroupId));
                    }
                }

            } catch (Exception e) {
                log.error("Error processing groups {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error fetching group data from NSU", e);
        }

    }
}
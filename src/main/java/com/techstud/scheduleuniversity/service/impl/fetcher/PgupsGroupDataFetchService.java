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
public class PgupsGroupDataFetchService implements GroupFetcherService {

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://rasp.pgups.ru/schedule/group";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl);
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)){
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                Document document = Jsoup.parse(responseBody);

                Elements elements = document.select("a.btn.btn-sm.btn");
                for (Element element : elements) {
                    String groupId = element.text();
                    String href = element.attr("href");

                    String universityGroupId = href.replace("https://rasp.pgups.ru/schedule/group/", "");

                    groupDataList.add(new GroupData(groupId, universityGroupId));
                }

                groupDataList = groupDataList.stream()
                        .filter(this::isValidGroup)
                        .sorted(Comparator.comparing(GroupData::universityGroupId))
                        .collect(Collectors.toList());

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return groupDataList;
    }

    private boolean isValidGroup(GroupData group) {
        if (!group.universityGroupId().matches("\\d+")) {
            log.warn("Filtered out non-numeric group: {}", group);
            return false;
        }

        return true;
    }
}

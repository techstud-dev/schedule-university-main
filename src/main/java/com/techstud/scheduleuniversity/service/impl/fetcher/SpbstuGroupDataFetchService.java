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
import java.util.List;

@Service
@Slf4j
public class SpbstuGroupDataFetchService implements GroupFetcherService {

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://ruz.spbstu.ru";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                Document document = Jsoup.parse(responseBody);

                Elements faculties = document.select("li.faculty-list__item > a.faculty-list__link");
                for (Element faculty : faculties) {
                    String facultyLink = faculty.attr("href");
                    parseFacultyGroups(httpClient, facultyLink, groupDataList);
                }

            }catch (Exception e) {
                log.error(e.getMessage());
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return groupDataList;
    }

    private void parseFacultyGroups(CloseableHttpClient httpClient, String facultyLink, List<GroupData> groupDataList) {
        String facultyUrl = "https://ruz.spbstu.ru" + facultyLink;

        try {
            HttpGet httpGet = new HttpGet(facultyUrl);

            try (CloseableHttpResponse facultyResponse = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(facultyResponse.getEntity(), StandardCharsets.UTF_8);
                Document document = Jsoup.parse(responseBody);

                Elements elements = document.select("li.groups-list__item > a.groups-list__link");
                for (Element element : elements) {
                    String href = element.attr("href");
                    String groupId = element.text();
                    String universityGroupId = href.replace(facultyLink + "/", "");

                    groupDataList.add(new GroupData(groupId, universityGroupId));
                }

            } catch (Exception e) {
                log.error(e.getMessage());
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

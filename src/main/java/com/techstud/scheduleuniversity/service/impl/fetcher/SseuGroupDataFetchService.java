package com.techstud.scheduleuniversity.service.impl.fetcher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstud.scheduleuniversity.dto.fetcher.GroupData;
import com.techstud.scheduleuniversity.dto.fetcher.api.response.SseuApiGroupDataResponse;
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
import java.util.List;

@Service("SSEU_GROUP_FETCHER")
@Slf4j
public class SseuGroupDataFetchService implements GroupFetcherService {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://lms3.sseu.ru/api/v1/schedule-board/groups";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getCode() == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    List<SseuApiGroupDataResponse> apiResponses = mapper.readValue(responseBody, new TypeReference<>() {
                    });
                    groupDataList = mapApiResponseToGroupList(apiResponses);
                } else {
                    log.error("Response SSEU api status code {}", response.getCode());
                }
            }
        } catch (Exception e) {
            log.error("Error fetching group data from SSEU", e);
        }
        return groupDataList;
    }

    private List<GroupData> mapApiResponseToGroupList(List<SseuApiGroupDataResponse> sseuApiGroupDataResponseList) {
        List<GroupData> groupDataList = new ArrayList<>();

        sseuApiGroupDataResponseList.forEach(sseuApiGroupDataResponse -> {
            GroupData groupData = new GroupData(sseuApiGroupDataResponse.name(), sseuApiGroupDataResponse.id().toString());
            groupDataList.add(groupData);
        });

        return groupDataList;
    }
}

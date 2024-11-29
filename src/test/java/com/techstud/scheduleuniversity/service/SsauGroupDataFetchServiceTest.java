package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dto.fetcher.GroupData;
import com.techstud.scheduleuniversity.service.impl.SsauGroupDataFetchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("dev")
@ExtendWith(MockitoExtension.class)
@Slf4j
public class SsauGroupDataFetchServiceTest {

    private GroupFetcherService ssauGroupDataFetchService;

    @BeforeEach
    public void setUp() {
        ssauGroupDataFetchService = new SsauGroupDataFetchService();
    }

    @Test
    public void testFetchGroupData() {
        List<GroupData> groupDataList = ssauGroupDataFetchService.fetchGroupsData();
        log.info("Group data list: {}", groupDataList);
        Assertions.assertNotNull(groupDataList);
    }
}

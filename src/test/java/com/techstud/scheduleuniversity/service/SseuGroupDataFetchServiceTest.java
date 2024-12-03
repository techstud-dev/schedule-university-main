package com.techstud.scheduleuniversity.service;


import com.google.gson.Gson;
import com.techstud.scheduleuniversity.dto.fetcher.GroupData;
import com.techstud.scheduleuniversity.service.impl.fetcher.SseuGroupDataFetchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@Slf4j
@ActiveProfiles("dev")
public class SseuGroupDataFetchServiceTest {
    private GroupFetcherService ssauGroupDataFetchService;

    @BeforeEach
    public void setUp() {
        ssauGroupDataFetchService = new SseuGroupDataFetchService();
    }

    @Test
    public void testFetchGroupData() {
        List<GroupData> groupDataList = ssauGroupDataFetchService.fetchGroupsData();
        String resultJson = new Gson().toJson(groupDataList);
        log.info("Group data list: {}", resultJson);
        Assertions.assertNotNull(groupDataList);
    }
}

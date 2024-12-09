package com.techstud.scheduleuniversity.service;

import com.google.gson.Gson;
import com.techstud.scheduleuniversity.dto.fetcher.GroupData;
import com.techstud.scheduleuniversity.service.impl.fetcher.PgupsGroupDataFetchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("dev")
@Slf4j
public class PgupsGroupDataFetchServiceTest {

    private GroupFetcherService pgupsGroupDataFetchService;

    @BeforeEach
    public void setUp() {
        pgupsGroupDataFetchService = new PgupsGroupDataFetchService();
    }

    @Test
    public void testFetchGroupData() {
        List<GroupData> groupDataList = pgupsGroupDataFetchService.fetchGroupsData();
        String resultJson = new Gson().toJson(groupDataList);
        log.info("Group data list: {}", resultJson);
        Assertions.assertNotNull(groupDataList);
    }
}

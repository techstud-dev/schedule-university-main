package com.techstud.scheduleuniversity.service;

import com.google.gson.Gson;
import com.techstud.scheduleuniversity.dto.fetcher.GroupData;
import com.techstud.scheduleuniversity.service.impl.fetcher.MephiGroupDataFetchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("dev")
@Slf4j
@Disabled
public class MephiGroupDataFetchServiceTest {

    private GroupFetcherService mephiGroupDataFetchService;

    @BeforeEach
    public void setUp() {
        mephiGroupDataFetchService = new MephiGroupDataFetchService();
    }

    @Test
    public void testFetchGroupData() {
        List<GroupData> groupDataList = mephiGroupDataFetchService.fetchGroupsData();
        String resultJson = new Gson().toJson(groupDataList);
        System.out.println(resultJson);
        log.info("Group data list: {}", resultJson);
        Assertions.assertNotNull(groupDataList);
    }
}

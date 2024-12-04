package com.techstud.scheduleuniversity.service.impl.fetcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstud.scheduleuniversity.dto.fetcher.GroupData;
import com.techstud.scheduleuniversity.service.GroupFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TltsuGroupDataFetchService implements GroupFetcherService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();
        String baseUrl = "https://its.tltsu.ru/api/schedule/group";

        return List.of();
    }
}

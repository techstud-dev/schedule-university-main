package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dto.fetcher.GroupData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

@Component
@Slf4j
@RequiredArgsConstructor
public class GroupFetchServiceFacade {

    private final GroupFetchServiceFactory groupFetchServiceFactory;

    @Async
    public Future<List<GroupData>> asyncParseGroups(String universityName) {
        log.info("Received task for fetching groups for university: {}", universityName);
        return new FutureTask<>(() -> {
            List<GroupData> groupDataList;
            try {
                groupDataList = groupFetchServiceFactory.getGroupFetcherService(universityName).fetchGroupsData();
            } catch (Exception e) {
                log.error("Error while fetching groups", e);
                return null;
            }
            return groupDataList;
        });
    }
}

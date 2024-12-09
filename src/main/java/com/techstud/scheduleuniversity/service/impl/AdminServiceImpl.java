package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dao.entity.University;
import com.techstud.scheduleuniversity.dao.entity.UniversityGroup;
import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.UpdateGroupDataTask;
import com.techstud.scheduleuniversity.dto.fetcher.GroupData;
import com.techstud.scheduleuniversity.repository.jpa.UniversityGroupRepository;
import com.techstud.scheduleuniversity.repository.jpa.UniversityRepository;
import com.techstud.scheduleuniversity.service.AdminService;
import com.techstud.scheduleuniversity.service.GroupFetchServiceFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Future;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UniversityRepository universityRepository;
    private final UniversityGroupRepository universityGroupRepository;
    private final GroupFetchServiceFacade groupFetcherServiceFacade;

    @Override
    @Async
    public boolean updateGroupsData(ApiRequest<UpdateGroupDataTask> updateTask) {
        boolean resultFlag = true;
        String universityName = updateTask.getData().getUniversityName();
        try {
            Future<List<GroupData>> groupDataFuture = groupFetcherServiceFacade
                    .asyncParseGroups(universityName);
            List<GroupData> groupDataList = groupDataFuture.get();
            if (groupDataList != null) {
                University university = universityRepository.findByShortName(universityName).orElseThrow(() ->
                         new NoSuchElementException("Not found university " + universityName));
                List<UniversityGroup> groups = groupDataList
                        .stream()
                        .map(groupData ->
                                new UniversityGroup(university, groupData.groupCode(), groupData.universityGroupId()))
                        .toList();
                List<UniversityGroup> savedGroups = universityGroupRepository.saveAll(groups);
            }
        } catch (Exception e) {
            log.error("Error while updating groups data", e);
            resultFlag = false;
        }
        return resultFlag;
    }
}

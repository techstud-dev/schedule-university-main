package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.UpdateGroupDataTask;
import com.techstud.scheduleuniversity.dto.fetcher.GroupData;
import com.techstud.scheduleuniversity.entity.Group;
import com.techstud.scheduleuniversity.entity.University;
import com.techstud.scheduleuniversity.repository.GroupRepository;
import com.techstud.scheduleuniversity.repository.UniversityRepository;
import com.techstud.scheduleuniversity.service.AdminService;
import com.techstud.scheduleuniversity.service.GroupFetchServiceFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UniversityRepository universityRepository;
    private final GroupRepository groupRepository;
    private final GroupFetchServiceFacade groupFetcherServiceFacade;

    @Override
    @Async
    public boolean updateGroupsData(ApiRequest<UpdateGroupDataTask> updateTask) {
        boolean resultFlag = true;
        String universityName = updateTask.getData().getUniversityName();
        try {

            Future<List<GroupData>> groupDataFuture = groupFetcherServiceFacade.asyncParseGroups(universityName);
            List<GroupData> groupDataList = groupDataFuture.get();

            if (groupDataList != null && !groupDataList.isEmpty()) {

                University university = universityRepository.findByShortName(universityName)
                        .orElseThrow(() ->
                                new NoSuchElementException("Not found university " + universityName));

                List<Group> existingGroups = groupRepository.findAllByUniversity(university);

                Map<String, Group> existingGroupsMap = existingGroups.stream()
                        .collect(Collectors.toMap(
                                g -> g.getUniversityGroupId() + "_" + g.getGroupCode(),
                                Function.identity()
                        ));

                List<Group> groupsToSave = new ArrayList<>();


                for (GroupData groupData : groupDataList) {
                    String key = groupData.universityGroupId() + "_" + groupData.groupCode();
                    Group group = existingGroupsMap.get(key);

                    if (group != null) {

                        group.setUniversityGroupId(groupData.universityGroupId());
                        group.setGroupCode(groupData.groupCode());

                        groupsToSave.add(group);
                    } else {

                        Group newGroup = new Group(university, groupData.groupCode(), groupData.universityGroupId());
                        groupsToSave.add(newGroup);
                    }
                }

                groupRepository.saveAll(groupsToSave);
            }
        } catch (Exception e) {
            log.error("Error while updating groups data", e);
            resultFlag = false;
        }
        return resultFlag;
    }
}


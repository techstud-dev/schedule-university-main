package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.UpdateGroupDataTask;
import com.techstud.scheduleuniversity.repository.jpa.UniversityGroupRepository;
import com.techstud.scheduleuniversity.repository.jpa.UniversityRepository;
import com.techstud.scheduleuniversity.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UniversityRepository universityRepository;
    private final UniversityGroupRepository universityGroupRepository;

    @Override
    public boolean updateGroupsData(ApiRequest<UpdateGroupDataTask> updateTask) {
        return false;
    }
}

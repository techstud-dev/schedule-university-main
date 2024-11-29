package com.techstud.scheduleuniversity.controller;

import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.UpdateGroupDataTask;
import com.techstud.scheduleuniversity.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/groups/update")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateGroups(ApiRequest<UpdateGroupDataTask> updateTask) {
        adminService.updateGroupsData(updateTask);
    }
}

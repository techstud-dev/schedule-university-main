package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.UpdateGroupDataTask;

public interface AdminService {

    boolean updateGroupsData(ApiRequest<UpdateGroupDataTask> updateTask);
}

package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.entity.Group;
import com.techstud.scheduleuniversity.entity.University;

import java.util.List;

public interface GroupService {

    Group saveOrUpdate(Group group);

    List<Group> saveOrUpdateAll(List<Group> groups);

    Group findByUniversityAndGroupCode(String groupCode, University university);

}

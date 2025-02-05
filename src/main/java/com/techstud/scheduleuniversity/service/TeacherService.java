package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.entity.Teacher;

import java.util.List;

public interface TeacherService {

    Teacher saveOrUpdate(Teacher teacher);

    List<Teacher> saveOrUpdateAll(List<Teacher> teachers);

}

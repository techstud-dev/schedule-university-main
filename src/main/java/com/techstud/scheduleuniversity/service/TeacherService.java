package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.entity.Teacher;
import com.techstud.scheduleuniversity.entity.University;

import java.util.List;

public interface TeacherService {

    Teacher saveOrUpdate(Teacher teacher);

    List<Teacher> saveOrUpdateAll(List<Teacher> teachers);

    Teacher findByUniversityAndTeacherNameAndLastNameAndFirstNameAndMiddleName(University university, String teacherName, String lastName, String firstName, String middleName);
}

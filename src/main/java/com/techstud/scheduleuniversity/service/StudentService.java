package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.entity.Group;
import com.techstud.scheduleuniversity.entity.Student;
import com.techstud.scheduleuniversity.entity.Teacher;

import java.util.List;

public interface StudentService {
    Student findByUsername(String username);
    List<Student> findAllByGroup(Group group);

    Student saveOrUpdate(Student student);

    List<Student> saveOrUpdateAll(List<Student> students);
}

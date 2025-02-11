package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.entity.Teacher;
import com.techstud.scheduleuniversity.repository.TeacherRepository;
import com.techstud.scheduleuniversity.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TransientObjectException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;

    @Override
    @Transactional
    public Teacher saveOrUpdate(Teacher teacher) {

        if (teacher.getUniversity().getId() == null) {
            throw new TransientObjectException("University in teacher: " + teacher + "out of context or not exists in db");
        }

        teacherRepository.findByUniversityAndTeacherNameAndLastNameAndFirstNameAndMiddleName(
                        teacher.getUniversity(), teacher.getTeacherName(), teacher.getLastName(), teacher.getFirstName(), teacher.getMiddleName())
                .ifPresent(foundedTeacher -> teacher.setId(foundedTeacher.getId()));
        return teacherRepository.save(teacher);
    }

    @Override
    @Transactional
    public List<Teacher> saveOrUpdateAll(List<Teacher> teachers) {
        List<Teacher> savedTeachers = new ArrayList<>();
        teachers.forEach(teacher -> savedTeachers.add(teacherRepository.save(teacher)));
        return savedTeachers;
    }
}

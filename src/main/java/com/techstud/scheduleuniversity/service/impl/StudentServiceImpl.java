package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.entity.Group;
import com.techstud.scheduleuniversity.entity.Student;
import com.techstud.scheduleuniversity.repository.StudentRepository;
import com.techstud.scheduleuniversity.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public Student findByUsername(String username) {
        return studentRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Student with username: '" + username + "' not found"));
    }

    @Override
    @Transactional
    public List<Student> findAllByGroup(Group group) {
        if (group == null) {
            return Collections.emptyList();
        }
        return studentRepository.findByGroup(group);
    }
}

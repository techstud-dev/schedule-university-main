package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.entity.Schedule;
import com.techstud.scheduleuniversity.repository.*;
import com.techstud.scheduleuniversity.service.ScheduleCascadeOperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleCascadeOperationServiceImpl implements ScheduleCascadeOperationService {

    private final UniversityRepository universityRepository;
    private final TimeSheetRepository timeSheetRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final PlaceRepository placeRepository;
    private final LessonRepository lessonRepository;
    private final GroupRepository groupRepository;


    @Override
    @Transactional
    public Schedule cascadeSave(Schedule schedule) {

        return null;
    }
}

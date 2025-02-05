package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.entity.Schedule;
import com.techstud.scheduleuniversity.repository.ScheduleRepository;
import com.techstud.scheduleuniversity.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleCascadeSaveService implements CascadeSaveService<Schedule> {

    private final TimeSheetService timeSheetService;
    private final TeacherService teacherService;
    private final PlaceService placeService;
    private final LessonService lessonService;
    private final GroupService groupService;
    private final ScheduleRepository scheduleRepository;

    @Override
    public Schedule cascadeSave(Schedule source) {
        validateInputObject(source);
        return null;
    }

    private void validateInputObject(Schedule schedule) {

    }
}

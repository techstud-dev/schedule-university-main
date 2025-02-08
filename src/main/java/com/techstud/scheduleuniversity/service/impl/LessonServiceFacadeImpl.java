package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dto.MappingScheduleItemDto;
import com.techstud.scheduleuniversity.dto.MappingScheduleParserDto;
import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.entity.*;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.ParserResponseTimeoutException;
import com.techstud.scheduleuniversity.exception.ResourceExistsException;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;
import com.techstud.scheduleuniversity.mapper.Mapper;
import com.techstud.scheduleuniversity.mapper.MultipleMapper;
import com.techstud.scheduleuniversity.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LessonServiceFacadeImpl implements LessonServiceFacade {

    private final StudentService studentService;
    private final MultipleMapper<Lesson, EntityModel<ScheduleItem>> lessonMapper;
    private final Mapper<MappingScheduleItemDto, Lesson> scheduleItemLessonMultipleMapper;
    private final LessonService lessonService;
    private final Mapper<Schedule, EntityModel<ScheduleApiResponse>> scheduleMapper;
    private final UniversityService universityService;

    @Override
    @Transactional(readOnly = true)
    public CollectionModel<EntityModel<ScheduleItem>> getLessonsByStudentAndScheduleDay(String username, String dayOfWeek, boolean isEvenWeek)
            throws ScheduleNotFoundException {
        Student student = studentService.findByUsername(username);
        if (student.getPersonalSchedule() != null) {
            List<Lesson> lessons = student.getPersonalSchedule().getLessonList()
                    .stream()
                    .filter(lesson -> false)
                    .toList();

            return lessonMapper.mapAll(lessons);
        }

        Group group = student.getGroup();
        if (group.getGroupSchedule() != null) {
            List<Lesson> lessons = group.getGroupSchedule().getLessonList()
                    .stream()
                    .filter(lesson -> false)
                    .toList();

            return lessonMapper.mapAll(lessons);
        }

        throw new ScheduleNotFoundException("Schedule not found for student with username: " + username);
    }

    @Override
    @Transactional(rollbackFor = ResourceExistsException.class)
    public EntityModel<ScheduleApiResponse> createScheduleDay(List<ScheduleItem> savedScheduleItems, String username)
            throws ResourceExistsException, ScheduleNotFoundException {
        Student student = studentService.findByUsername(username);
        if (student.getPersonalSchedule() != null) {
            checkExistsScheduleDay(student.getPersonalSchedule(), savedScheduleItems);
            Student finalStudent = student;
            List<Lesson> lessons = savedScheduleItems.stream()
                    .map(scheduleItem -> MappingScheduleItemDto.builder()
                            .scheduleItem(scheduleItem)
                            .universityShortName(finalStudent.getGroup().getUniversity().getShortName())
                            .build())
                    .map(scheduleItemLessonMultipleMapper::map)
                    .toList();

            lessons.forEach(lesson -> {
                lesson.setTeacher(cascadeSaveTeacher(lesson.getTeacher()));
                lesson.setPlace(cascadeSavePlace(lesson.getPlace()));
                lesson.setGroups(lesson.getGroups().stream().map(this::cascadeSaveGroup).toList());
            });

            lessons = lessonService.saveOrUpdateAll(lessons);
            student.getPersonalSchedule().getLessonList().addAll(lessons);
            student = studentService.saveOrUpdate(student);

            return scheduleMapper.map(student.getPersonalSchedule());
        }
        throw new ScheduleNotFoundException("Personal schedule not found for student with username: " + username);
    }

    @Override
    @Transactional(rollbackFor = ScheduleNotFoundException.class)
    public EntityModel<ScheduleApiResponse> deleteLesson(String dayOfWeek, Long timeWindowId, String name)
            throws ScheduleNotFoundException {
        Student student = studentService.findByUsername(name);
        if (student.getPersonalSchedule() != null) {
            List<Lesson> lessons = student.getPersonalSchedule().getLessonList();
            lessons.removeIf(lesson -> false);
            student.getPersonalSchedule().setLessonList(lessons);
            student = studentService.saveOrUpdate(student);

            return scheduleMapper.map(student.getPersonalSchedule());
        }
        throw new ScheduleNotFoundException("Personal schedule not found for student with username: " + name);
    }

    @Override
    public EntityModel<ScheduleApiResponse> updateLesson(String dayOfWeek, Long timeWindowId, ScheduleItem data, String name) {
        return null;
    }

    @Override
    public EntityModel<ScheduleApiResponse> createLesson(ScheduleItem data, String name) {
        return null;
    }

    @Override
    public CollectionModel<EntityModel<ScheduleItem>> getLessonByStudentAndScheduleDayAndTimeWindow(String name, String dayOfWeek, Long timeWindowId) {
        return null;
    }

    @Override
    public EntityModel<ScheduleApiResponse> deleteScheduleDay(String dayOfWeek, String name, boolean isEvenWeek) {
        return null;
    }

    @Override
    public EntityModel<ScheduleApiResponse> updateScheduleDay(String dayOfWeek, List<ScheduleItem> data, String name, boolean isEvenWeek) {
        return null;
    }

    private void checkExistsScheduleDay(Schedule schedule, List<ScheduleItem> scheduleItems) throws ResourceExistsException {
        DayOfWeek dayOfWeek = mapRuDayOfWeek(scheduleItems.get(0).getDayOfWeek());
        schedule.getLessonList().forEach(lesson ->{
            if (lesson.getDayOfWeek().equals(dayOfWeek)) {
                try {
                    throw new ResourceExistsException("Schedule day already exists");
                } catch (ResourceExistsException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private DayOfWeek mapRuDayOfWeek(String dayOfWeek) {
        return switch (dayOfWeek) {
            case "Понедельник" -> DayOfWeek.MONDAY;
            case "Вторник" -> DayOfWeek.TUESDAY;
            case "Среда" -> DayOfWeek.WEDNESDAY;
            case "Четверг" -> DayOfWeek.THURSDAY;
            case "Пятница" -> DayOfWeek.FRIDAY;
            case "Суббота" -> DayOfWeek.SATURDAY;
            case "Воскресенье" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Unknown day of week: " + dayOfWeek);
        };
    }

    private Teacher cascadeSaveTeacher(Teacher teacher) {
        String universityShortName = teacher.getUniversity().getShortName();
        University university = universityService.findByShortName(universityShortName);
        teacher.setUniversity(university);
        return teacher;
    }

    private Place cascadeSavePlace(Place place) {
        String universityShortName = place.getUniversity().getShortName();
        University university = universityService.findByShortName(universityShortName);
        place.setUniversity(university);
        return place;
    }

    private Group cascadeSaveGroup(Group group) {
        String universityShortName = group.getUniversity().getShortName();
        University university = universityService.findByShortName(universityShortName);
        group.setUniversity(university);
        return group;
    }
}

package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dto.CreateScheduleDto;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.entity.*;
import com.techstud.scheduleuniversity.exception.*;
import com.techstud.scheduleuniversity.mapper.Mapper;
import com.techstud.scheduleuniversity.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceFacadeImpl implements ScheduleServiceFacade {

    private final StudentService studentService;
    private final Mapper<Schedule, EntityModel<ScheduleApiResponse>> scheduleMapper;
    private final ParserService parserService;
    private final Mapper<ScheduleParserResponse, Schedule> parserResponseMapper;
    private final GroupService groupService;
    private final PlaceService placeService;
    private final TeacherService teacherService;
    private final LessonService lessonService;
    private final ScheduleService scheduleService;
    private final Mapper<CreateScheduleDto, Schedule> dtoScheduleMapper;
    private final UniversityService universityService;

    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> importSchedule(ImportDto importDto, String username)
            throws ParserException, ParserResponseTimeoutException, RequestException {
        Student student = studentService.findByUsername(username);

        if (!student.getGroup().getGroupCode().equalsIgnoreCase(importDto.getGroupCode())) {
            throw new RequestException("Group in import dto +'" + importDto.getGroupCode() + "' and student group '"
                    + student.getGroup().getGroupCode() + "' not match!");
        }

        Schedule returnedSchedule = student.getPersonalSchedule();

        if (returnedSchedule != null) {
            log.info("For student '{}' found personal schedule with id = {}", username, returnedSchedule.getId());
            return scheduleMapper.map(returnedSchedule);
        }

        returnedSchedule = student.getGroup().getGroupSchedule();

        if (returnedSchedule != null) {
            log.info("For student '{}' found group schedule with id = {}", username, returnedSchedule.getId());
            return scheduleMapper.map(returnedSchedule);
        }

        ParsingTask parsingTask = ParsingTask.builder()
                .universityName(importDto.getUniversityShortName())
                .groupId(importDto.getGroupCode())
                .build();

        ScheduleParserResponse parserResponse = parserService.parseSchedule(parsingTask, student);

        returnedSchedule = parserResponseMapper.map(parserResponse);

        returnedSchedule = cascadeSave(returnedSchedule);

        if (returnedSchedule != null) {
            Group studentGroup = student.getGroup();
            studentGroup.setGroupSchedule(returnedSchedule);
            studentGroup = groupService.saveOrUpdate(studentGroup);
            student.setGroup(studentGroup);
            student.setPersonalSchedule(returnedSchedule);
            studentService.saveOrUpdate(student);
            log.info("For student '{}' parsed schedule from university '{}', group '{}', scheduleId: {}",
                    username, importDto.getUniversityShortName(), importDto.getGroupCode(), returnedSchedule.getId());
            return scheduleMapper.map(returnedSchedule);
        }
        throw new ParserException("Null returned schedule!");
    }

    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> forceImportSchedule(ImportDto importDto, String username)
            throws ParserException, RequestException, ParserResponseTimeoutException {

        Student student = studentService.findByUsername(username);

        if (!student.getGroup().getGroupCode().equalsIgnoreCase(importDto.getGroupCode())) {
            throw new RequestException("Group in import dto +'" + importDto.getGroupCode() + "' and student group '"
                    + student.getGroup().getGroupCode() + "' not match!");
        }

        ParsingTask parsingTask = ParsingTask.builder()
                .universityName(importDto.getUniversityShortName())
                .groupId(importDto.getGroupCode())
                .build();

        ScheduleParserResponse parserResponse = parserService.parseSchedule(parsingTask, student);

        Schedule returnedSchedule = parserResponseMapper.map(parserResponse);

        returnedSchedule = cascadeSave(returnedSchedule);

        if (returnedSchedule != null) {
            student.setPersonalSchedule(returnedSchedule);
            studentService.saveOrUpdate(student);
            log.info("For student '{}' parsed schedule from university '{}', group '{}', scheduleId: {}",
                    username, importDto.getUniversityShortName(), importDto.getGroupCode(), returnedSchedule.getId());
            return scheduleMapper.map(returnedSchedule);
        }

        throw new ParserException("Null returned schedule!");
    }

    @Override
    public EntityModel<ScheduleApiResponse> createSchedule(CreateScheduleDto saveDto, String username) throws ResourceExistsException {
        Student student = studentService.findByUsername(username);
        if (student.getPersonalSchedule() != null) {
            throw new ResourceExistsException("Student already has personal schedule!");
        }

        Schedule schedule = dtoScheduleMapper.map(saveDto);
        schedule = cascadeSave(schedule);
        student.setPersonalSchedule(schedule);
        studentService.saveOrUpdate(student);

        return scheduleMapper.map(schedule);
    }

    @Override
    public void deleteSchedule(Long id, String username) throws ScheduleNotFoundException {
        Student student = studentService.findByUsername(username);

        if (student.getPersonalSchedule() == null) {
            throw new ScheduleNotFoundException("Student has no personal schedule!");
        }

        if (!student.getPersonalSchedule().getId().equals(id)) {
            throw new ScheduleNotFoundException("Student has no personal schedule with id = " + id);
        }

        student.setPersonalSchedule(null);
        scheduleService.deleteById(id);
        studentService.saveOrUpdate(student);
    }

    @Override
    public EntityModel<ScheduleApiResponse> getScheduleById(Long scheduleId) throws ScheduleNotFoundException {
        Schedule schedule = scheduleService.findById(scheduleId);
        return scheduleMapper.map(schedule);
    }

    @Override
    public EntityModel<ScheduleApiResponse> getScheduleByStudent(String username) throws ParserException, ParserResponseTimeoutException {
        Student student = studentService.findByUsername(username);
        Schedule returnedSchedule = student.getPersonalSchedule();
        if (returnedSchedule == null) {
            returnedSchedule = student.getGroup().getGroupSchedule();
        }

        if (returnedSchedule == null) {
            University university = student.getGroup().getUniversity();
            ParsingTask parsingTask = ParsingTask.builder()
                    .universityName(university.getShortName())
                    .groupId(student.getGroup().getUniversityGroupId())
                    .build();

            ScheduleParserResponse parserResponse = parserService.parseSchedule(parsingTask, student);
            returnedSchedule = parserResponseMapper.map(parserResponse);
        }

        if (returnedSchedule != null) {
           returnedSchedule = cascadeSave(returnedSchedule);
              student.setPersonalSchedule(returnedSchedule);
              Group group = student.getGroup();
              group.setGroupSchedule(returnedSchedule);
              group = groupService.saveOrUpdate(group);
              student.setGroup(group);
                studentService.saveOrUpdate(student);
        }

        return scheduleMapper.map(returnedSchedule);
    }

    private Schedule cascadeSave(Schedule schedule) {
        List<Lesson> savedSchedules = schedule.getLessonList()
                .stream()
                .map(lesson -> {
                    lesson.setPlace(placeService.saveOrUpdate(cascadeSavePlace(lesson.getPlace())));
                    lesson.setTeacher(teacherService.saveOrUpdate(cascadeSaveTeacher(lesson.getTeacher())));
                    lesson.setGroups(lesson.getGroups()
                            .stream()
                            .map(groupService::saveOrUpdate)
                            .collect(Collectors.toList()));
                    return lessonService.saveOrUpdate(lesson);
                }).toList();
        schedule.setLessonList(savedSchedules);
        return scheduleService.saveOrUpdate(schedule);
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
}
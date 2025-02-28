package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dto.*;
import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.entity.Group;
import com.techstud.scheduleuniversity.entity.Lesson;
import com.techstud.scheduleuniversity.entity.Place;
import com.techstud.scheduleuniversity.entity.Schedule;
import com.techstud.scheduleuniversity.entity.Student;
import com.techstud.scheduleuniversity.entity.Teacher;
import com.techstud.scheduleuniversity.entity.University;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.ParserResponseTimeoutException;
import com.techstud.scheduleuniversity.exception.RequestException;
import com.techstud.scheduleuniversity.exception.ResourceExistsException;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;
import com.techstud.scheduleuniversity.mapper.Mapper;
import com.techstud.scheduleuniversity.service.GroupService;
import com.techstud.scheduleuniversity.service.LessonService;
import com.techstud.scheduleuniversity.service.ParserService;
import com.techstud.scheduleuniversity.service.PlaceService;
import com.techstud.scheduleuniversity.service.ScheduleService;
import com.techstud.scheduleuniversity.service.ScheduleServiceFacade;
import com.techstud.scheduleuniversity.service.StudentService;
import com.techstud.scheduleuniversity.service.TeacherService;
import com.techstud.scheduleuniversity.service.TimeSheetService;
import com.techstud.scheduleuniversity.service.UniversityService;
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
    private final Mapper<MappingScheduleParserDto, Schedule> parserResponseMapper;
    private final GroupService groupService;
    private final PlaceService placeService;
    private final TeacherService teacherService;
    private final LessonService lessonService;
    private final ScheduleService scheduleService;
    private final Mapper<CreateScheduleDto, Schedule> dtoScheduleMapper;
    private final UniversityService universityService;
    private final TimeSheetService timeSheetService;

    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> importSchedule(ImportDto importDto, String username)
            throws ParserException, ParserResponseTimeoutException, RequestException {
        Student student = studentService.findByUsername(username);
        Group studentGroup = student.getGroup();
        if (studentGroup == null) {
            throw new RequestException("Student group is not available!");
        }
        if (!studentGroup.getGroupCode().equalsIgnoreCase(importDto.getGroupCode())) {
            throw new RequestException("Mismatch between import dto group '" + importDto.getGroupCode() +
                    "' and student's group '" + studentGroup.getGroupCode() + "'");
        }

        Schedule schedule = student.getPersonalSchedule();
        if (schedule != null) {
            log.info("Personal schedule found for student '{}' with id {}", username, schedule.getId());
            initializeSchedule(schedule);
            return scheduleMapper.map(schedule);
        }

        schedule = studentGroup.getGroupSchedule();
        if (schedule != null) {
            log.info("Group schedule found for student '{}' with id {}", username, schedule.getId());
            initializeSchedule(schedule);
            return scheduleMapper.map(schedule);
        }

        schedule = parseAndSaveSchedule(importDto.getUniversityShortName(), importDto.getGroupCode(), student);
        if (schedule == null) {
            throw new ParserException("Parsed schedule is null!");
        }

        studentGroup.setGroupSchedule(schedule);
        groupService.saveOrUpdate(studentGroup);
        student.setGroup(studentGroup);
        student.setPersonalSchedule(schedule);
        studentService.saveOrUpdate(student);
        log.info("Schedule parsed for student '{}' from university '{}' and group '{}', scheduleId: {}",
                username, importDto.getUniversityShortName(), importDto.getGroupCode(), schedule.getId());
        initializeSchedule(schedule);
        return scheduleMapper.map(schedule);
    }

    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> forceImportSchedule(ImportDto importDto, String username)
            throws ParserException, RequestException, ParserResponseTimeoutException {
        Student student = studentService.findByUsername(username);
        Group studentGroup = student.getGroup();
        if (studentGroup == null) {
            throw new RequestException("Student group is not available!");
        }
        if (!studentGroup.getGroupCode().equalsIgnoreCase(importDto.getGroupCode())) {
            throw new RequestException("Mismatch between import dto group '" + importDto.getGroupCode() +
                    "' and student's group '" + studentGroup.getGroupCode() + "'");
        }
        Schedule schedule = parseAndSaveSchedule(importDto.getUniversityShortName(), importDto.getGroupCode(), student);
        if (schedule == null) {
            throw new ParserException("Parsed schedule is null!");
        }
        student.setPersonalSchedule(schedule);
        studentService.saveOrUpdate(student);
        log.info("Schedule force-imported for student '{}' from university '{}' and group '{}', scheduleId: {}",
                username, importDto.getUniversityShortName(), importDto.getGroupCode(), schedule.getId());
        initializeSchedule(schedule);
        return scheduleMapper.map(schedule);
    }

    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> createSchedule(CreateScheduleDto saveDto, String username)
            throws ResourceExistsException {
        Student student = studentService.findByUsername(username);
        if (student.getPersonalSchedule() != null) {
            throw new ResourceExistsException("Student already has a personal schedule!");
        }
        Schedule schedule = dtoScheduleMapper.map(saveDto);
        schedule = cascadeSave(schedule);
        student.setPersonalSchedule(schedule);
        studentService.saveOrUpdate(student);
        initializeSchedule(schedule);
        log.info("Personal schedule created for student '{}' with scheduleId: {}", username, schedule.getId());
        return scheduleMapper.map(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id, String username) throws ScheduleNotFoundException {
        Student student = studentService.findByUsername(username);
        Schedule personalSchedule = student.getPersonalSchedule();
        if (personalSchedule == null) {
            throw new ScheduleNotFoundException("Student does not have a personal schedule!");
        }
        if (!personalSchedule.getId().equals(id)) {
            throw new ScheduleNotFoundException("Student does not have a personal schedule with id " + id);
        }
        student.setPersonalSchedule(null);
        scheduleService.deleteById(id);
        studentService.saveOrUpdate(student);
        log.info("Deleted schedule with id {} for student '{}'", id, username);
    }

    @Override
    @Transactional(readOnly = true)
    public EntityModel<ScheduleApiResponse> getScheduleById(Long scheduleId) throws ScheduleNotFoundException {
        Schedule schedule = scheduleService.findById(scheduleId);
        if (schedule == null) {
            throw new ScheduleNotFoundException("Schedule not found with id " + scheduleId);
        }
        initializeSchedule(schedule);
        log.info("Retrieved schedule with id {}", scheduleId);
        return scheduleMapper.map(schedule);
    }

    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> getScheduleByStudent(String username)
            throws ParserException, ParserResponseTimeoutException {
        Student student = studentService.findByUsername(username);
        Schedule schedule = student.getPersonalSchedule();
        if (schedule == null) {
            Group studentGroup = student.getGroup();
            schedule = studentGroup.getGroupSchedule();
        }
        if (schedule == null) {
            University university = student.getGroup().getUniversity();
            schedule = parseAndSaveSchedule(university.getShortName(), student.getGroup().getUniversityGroupId(), student);
            if (schedule != null) {
                student.setPersonalSchedule(schedule);
                Group group = student.getGroup();
                group.setGroupSchedule(schedule);
                groupService.saveOrUpdate(group);
                student.setGroup(group);
                studentService.saveOrUpdate(student);
                log.info("Parsed schedule for student '{}' as no existing schedule was found", username);
            }
        }
        initializeSchedule(schedule);
        return scheduleMapper.map(schedule);
    }

    @Override
    public EntityModel<ScheduleApiResponse> updateSchedule(ApiRequest<UpdateScheduleRequest> request, String username, Long scheduleId) {
        Student student = studentService.findByUsername(username);
        Schedule schedule = student.getPersonalSchedule();

        return scheduleMapper.map(schedule);
    }

    /**
     * Parses a schedule using the parser service and performs cascade-save.
     */
    private Schedule parseAndSaveSchedule(String universityShortName, String groupCode, Student student)
            throws ParserException, ParserResponseTimeoutException {
        ParsingTask parsingTask = ParsingTask.builder()
                .universityName(universityShortName)
                .groupId(groupCode)
                .build();
        ScheduleParserResponse parserResponse = parserService.parseSchedule(parsingTask, student);
        MappingScheduleParserDto mappingTask = MappingScheduleParserDto.builder()
                .scheduleParserResponse(parserResponse)
                .universityShortName(universityShortName)
                .build();
        Schedule schedule = parserResponseMapper.map(mappingTask);
        return cascadeSave(schedule);
    }

    /**
     * Cascade-saves schedule and its associated entities.
     */
    private Schedule cascadeSave(Schedule schedule) {
        if (schedule.getLessonList() != null) {
            List<Lesson> savedLessons = schedule.getLessonList().stream().map(lesson -> {
                lesson.setPlace(placeService.saveOrUpdate(cascadeSavePlace(lesson.getPlace())));
                lesson.setTeacher(teacherService.saveOrUpdate(cascadeSaveTeacher(lesson.getTeacher())));
                lesson.setGroups(lesson.getGroups().stream()
                        .map(this::cascadeSaveGroup)
                        .map(groupService::saveOrUpdate)
                        .collect(Collectors.toList()));
                lesson.setTimeSheet(timeSheetService.saveOrUpdate(lesson.getTimeSheet()));
                return lessonService.saveOrUpdate(lesson);
            }).collect(Collectors.toList());
            schedule.setLessonList(savedLessons);
        }
        return scheduleService.saveOrUpdate(schedule);
    }

    private Teacher cascadeSaveTeacher(Teacher teacher) {
        if (teacher != null && teacher.getUniversity() != null) {
            String universityShortName = teacher.getUniversity().getShortName();
            University university = universityService.findByShortName(universityShortName);
            teacher.setUniversity(university);
        }
        return teacher;
    }

    private Place cascadeSavePlace(Place place) {
        if (place != null && place.getUniversity() != null) {
            String universityShortName = place.getUniversity().getShortName();
            University university = universityService.findByShortName(universityShortName);
            place.setUniversity(university);
        }
        return place;
    }

    private Group cascadeSaveGroup(Group group) {
        if (group != null && group.getUniversity() != null) {
            String universityShortName = group.getUniversity().getShortName();
            University university = universityService.findByShortName(universityShortName);
            group.setUniversity(university);
        }
        return group;
    }

    /**
     * Initializes lazy-loaded associations of the schedule to prevent LazyInitializationException.
     */
    private void initializeSchedule(Schedule schedule) {
        if (schedule != null && schedule.getLessonList() != null) {
            schedule.getLessonList().size();
            for (Lesson lesson : schedule.getLessonList()) {
                if (lesson.getTeacher() != null) {
                    lesson.getTeacher().getId();
                    if (lesson.getTeacher().getUniversity() != null) {
                        lesson.getTeacher().getUniversity().getShortName();
                    }
                }
                if (lesson.getPlace() != null) {
                    lesson.getPlace().getId();
                    if (lesson.getPlace().getUniversity() != null) {
                        lesson.getPlace().getUniversity().getShortName();
                    }
                }
                if (lesson.getTimeSheet() != null) {
                    lesson.getTimeSheet().getId();
                }
                if (lesson.getGroups() != null) {
                    lesson.getGroups().size();
                    for (Group group : lesson.getGroups()) {
                        group.getId();
                        if (group.getUniversity() != null) {
                            group.getUniversity().getShortName();
                        }
                    }
                }
            }
        }
    }
}

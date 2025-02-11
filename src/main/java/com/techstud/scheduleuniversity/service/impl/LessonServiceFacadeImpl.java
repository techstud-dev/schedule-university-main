package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dto.MappingScheduleItemDto;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.entity.*;
import com.techstud.scheduleuniversity.exception.ResourceExistsException;
import com.techstud.scheduleuniversity.exception.ResourceNotFoundException;
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
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private final TimeSheetService timeSheetService;
    private final TeacherService teacherService;
    private final PlaceService placeService;
    private final GroupService groupService;

    @Override
    @Transactional(readOnly = true)
    public CollectionModel<EntityModel<ScheduleItem>> getLessonsByStudentAndScheduleDay(String username, String dayOfWeek, boolean isEvenWeek)
            throws ScheduleNotFoundException {
        Student student = studentService.findByUsername(username);
        List<Lesson> lessons = null;

        if (student.getPersonalSchedule() != null) {
            // Force initialization of the schedule and its lessons
            initializeSchedule(student.getPersonalSchedule());
            lessons = student.getPersonalSchedule().getLessonList().stream()
                    .filter(lesson -> lesson.getDayOfWeek().equals(DayOfWeek.valueOf(dayOfWeek))
                            && lesson.isEvenWeek() == isEvenWeek)
                    .collect(Collectors.toList());
            log.info("Found {} lessons in personal schedule for student '{}'", lessons.size(), username);
        } else if (student.getGroup() != null && student.getGroup().getGroupSchedule() != null) {
            initializeSchedule(student.getGroup().getGroupSchedule());
            lessons = student.getGroup().getGroupSchedule().getLessonList().stream()
                    .filter(lesson -> lesson.getDayOfWeek().equals(mapRuDayOfWeek(dayOfWeek))
                            && lesson.isEvenWeek() == isEvenWeek)
                    .collect(Collectors.toList());
            log.info("Found {} lessons in group schedule for student '{}'", lessons.size(), username);
        } else {
            throw new ScheduleNotFoundException("Schedule not found for student with username: " + username);
        }

        initializeLessons(lessons);
        return lessonMapper.mapAll(lessons);
    }

    @Override
    @Transactional(rollbackFor = ResourceExistsException.class)
    public EntityModel<ScheduleApiResponse> createScheduleDay(List<ScheduleItem> savedScheduleItems, String username)
            throws ScheduleNotFoundException {
        Student student = studentService.findByUsername(username);
        if (student.getPersonalSchedule() == null) {
            throw new ScheduleNotFoundException("Personal schedule not found for student with username: " + username);
        }

        checkExistsScheduleDay(student.getPersonalSchedule(), savedScheduleItems);

        Student finalStudent = student;
        List<Lesson> newLessons = savedScheduleItems.stream()
                .map(scheduleItem -> MappingScheduleItemDto.builder()
                        .scheduleItem(scheduleItem)
                        .universityShortName(finalStudent.getGroup().getUniversity().getShortName())
                        .build())
                .map(scheduleItemLessonMultipleMapper::map)
                .collect(Collectors.toList());

        // Cascade-save associated entities (teacher, place, groups) for each lesson
        newLessons.forEach(lesson -> {
            lesson.setTeacher(cascadeSaveTeacher(lesson.getTeacher()));
            lesson.setPlace(cascadeSavePlace(lesson.getPlace()));
            lesson.setGroups(lesson.getGroups().stream()
                    .map(this::cascadeSaveGroup)
                    .collect(Collectors.toList()));
        });

        newLessons = lessonService.saveOrUpdateAll(newLessons);
        student.getPersonalSchedule().getLessonList().addAll(newLessons);
        student = studentService.saveOrUpdate(student);
        initializeSchedule(student.getPersonalSchedule());
        log.info("Created schedule day for student '{}' with {} new lessons", username, newLessons.size());
        return scheduleMapper.map(student.getPersonalSchedule());
    }

    @Override
    @Transactional(rollbackFor = ScheduleNotFoundException.class)
    public EntityModel<ScheduleApiResponse> deleteLesson(String dayOfWeek, Long timeWindowId, String username)
            throws ScheduleNotFoundException {
        Student student = studentService.findByUsername(username);
        if (student.getPersonalSchedule() == null) {
            throw new ScheduleNotFoundException("Personal schedule not found for student with username: " + username);
        }
        Schedule schedule = student.getPersonalSchedule();
        initializeSchedule(schedule);
        List<Lesson> lessons = schedule.getLessonList();

        int before = lessons.size();
        lessons.removeIf(lesson -> lesson.getDayOfWeek().equals(mapRuDayOfWeek(dayOfWeek))
                && lesson.getTimeSheet().getId().equals(timeWindowId));
        int after = lessons.size();
        log.info("Deleted {} lessons from schedule for student '{}'", (before - after), username);

        schedule.setLessonList(lessons);
        student.setPersonalSchedule(schedule);
        student = studentService.saveOrUpdate(student);
        initializeSchedule(student.getPersonalSchedule());
        return scheduleMapper.map(student.getPersonalSchedule());
    }

    @Override
    @Transactional(rollbackFor = ScheduleNotFoundException.class)
    public EntityModel<ScheduleApiResponse> updateLesson(String dayOfWeek, Long timeWindowId, ScheduleItem data, String username)
            throws ScheduleNotFoundException {
        Student student = studentService.findByUsername(username);
        if (student.getPersonalSchedule() == null) {
            throw new ScheduleNotFoundException("Personal schedule not found for student with username: " + username);
        }
        Schedule schedule = student.getPersonalSchedule();
        initializeSchedule(schedule);
        List<Lesson> lessons = schedule.getLessonList();

        // Remove existing lesson matching day and time window
        lessons.removeIf(lesson -> lesson.getDayOfWeek().equals(mapRuDayOfWeek(dayOfWeek))
                && lesson.getTimeSheet().getId().equals(timeWindowId));

        // Map new ScheduleItem to Lesson and add it
        Lesson newLesson = scheduleItemLessonMultipleMapper.map(MappingScheduleItemDto.builder()
                .scheduleItem(data)
                .universityShortName(student.getGroup().getUniversity().getShortName())
                .build());
        lessons.add(newLesson);

        lessons = lessonService.saveOrUpdateAll(lessons);
        schedule.setLessonList(lessons);
        student.setPersonalSchedule(schedule);
        studentService.saveOrUpdate(student);
        initializeSchedule(schedule);
        log.info("Updated lesson for student '{}' for day '{}' and timeWindowId {}", username, dayOfWeek, timeWindowId);
        return scheduleMapper.map(student.getPersonalSchedule());
    }

    @Override
    @Transactional(rollbackFor = {ScheduleNotFoundException.class, ResourceExistsException.class})
    public EntityModel<ScheduleApiResponse> createLesson(ScheduleItem data, String username)
            throws ScheduleNotFoundException, ResourceExistsException {
        Student student = studentService.findByUsername(username);
        if (student.getPersonalSchedule() == null) {
            throw new ScheduleNotFoundException("Personal schedule not found for student with username: " + username);
        }
        Schedule schedule = student.getPersonalSchedule();
        initializeSchedule(schedule);
        List<Lesson> lessons = schedule.getLessonList();

        TimeSheet timeSheet = timeSheetService.findByStandardPattern(data.getTime());
        if(timeSheet == null) {
            timeSheet = new TimeSheet();
            String[] times = data.getTime().replaceAll(" ", "").trim().split("-");
            timeSheet.setFromTime(LocalTime.parse(times[0]));
            timeSheet.setToTime(LocalTime.parse(times[1]));
            timeSheet = timeSheetService.saveOrUpdate(timeSheet);
        }
        TimeSheet finalTimeSheet = timeSheet;
        boolean isExists = lessons.stream()
                .anyMatch(lesson -> lesson.getDayOfWeek().equals(mapRuDayOfWeek(data.getDayOfWeek()))
                        && lesson.getTimeSheet().equals(finalTimeSheet)
                        && lesson.isEvenWeek() == data.isEven());

        if (isExists) {
            throw new ResourceExistsException("Lesson for current day and time sheet already exists");
        } else {
            Lesson newLesson = scheduleItemLessonMultipleMapper.map(MappingScheduleItemDto.builder()
                    .scheduleItem(data)
                    .universityShortName(student.getGroup().getUniversity().getShortName())
                    .build());

            newLesson = cascadeSaveLesson(newLesson);
            lessons.add(newLesson);
            lessons = lessonService.saveOrUpdateAll(lessons);
            schedule.setLessonList(lessons);
            student.setPersonalSchedule(schedule);
            studentService.saveOrUpdate(student);
            initializeSchedule(schedule);
            log.info("Created new lesson for student '{}' on day '{}'", username, data.getDayOfWeek());
            return scheduleMapper.map(student.getPersonalSchedule());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionModel<EntityModel<ScheduleItem>> getLessonByStudentAndScheduleDayAndTimeWindow(String username, String dayOfWeek, Long timeWindowId)
            throws ScheduleNotFoundException, ResourceNotFoundException {
        Student student = studentService.findByUsername(username);
        if (student.getPersonalSchedule() == null) {
            throw new ScheduleNotFoundException("Personal schedule not found for student with username: " + username);
        }
        initializeSchedule(student.getPersonalSchedule());
        List<Lesson> lessons = student.getPersonalSchedule().getLessonList().stream()
                .filter(lesson -> lesson.getDayOfWeek().equals(DayOfWeek.valueOf(dayOfWeek))
                        && lesson.getTimeSheet().getId().equals(timeWindowId))
                .collect(Collectors.toList());

        if (lessons.isEmpty()) {
            throw new ResourceNotFoundException("Lessons not found for student with username: " + username);
        }
        initializeLessons(lessons);
        log.info("Retrieved {} lessons for student '{}' on day '{}' and timeWindowId {}", lessons.size(), username, dayOfWeek, timeWindowId);
        return lessonMapper.mapAll(lessons);
    }

    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> deleteScheduleDay(String dayOfWeek, String username, boolean isEvenWeek)
            throws ScheduleNotFoundException {
        Student student = studentService.findByUsername(username);
        if (student.getPersonalSchedule() == null) {
            throw new ScheduleNotFoundException("Personal schedule not found for student with username: " + username);
        }
        Schedule schedule = student.getPersonalSchedule();
        initializeSchedule(schedule);
        List<Lesson> lessons = schedule.getLessonList();
        int before = lessons.size();
        lessons.removeIf(lesson -> lesson.getDayOfWeek().equals(mapRuDayOfWeek(dayOfWeek))
                && lesson.isEvenWeek() == isEvenWeek);
        int after = lessons.size();
        log.info("Deleted {} lessons from schedule day '{}' for student '{}'", (before - after), dayOfWeek, username);
        schedule.setLessonList(lessons);
        student.setPersonalSchedule(schedule);
        student = studentService.saveOrUpdate(student);
        initializeSchedule(schedule);
        return scheduleMapper.map(schedule);
    }

    @Override
    @Transactional
    public EntityModel<ScheduleApiResponse> updateScheduleDay(String dayOfWeek, List<ScheduleItem> data, String username, boolean isEvenWeek)
            throws ScheduleNotFoundException {
        Student student = studentService.findByUsername(username);
        if (student.getPersonalSchedule() == null) {
            throw new ScheduleNotFoundException("Personal schedule not found for student with username: " + username);
        }
        Schedule schedule = student.getPersonalSchedule();
        initializeSchedule(schedule);
        List<Lesson> lessons = schedule.getLessonList();
        // Remove lessons for the specified day and evenWeek flag
        lessons.removeIf(lesson -> lesson.getDayOfWeek().equals(mapRuDayOfWeek(dayOfWeek))
                && lesson.isEvenWeek() == isEvenWeek);

        // Map incoming schedule items to lessons and add them
        Student finalStudent = student;
        List<Lesson> newLessons = data.stream()
                .map(item -> MappingScheduleItemDto.builder()
                        .scheduleItem(item)
                        .universityShortName(finalStudent.getGroup().getUniversity().getShortName())
                        .build())
                .map(scheduleItemLessonMultipleMapper::map)
                .toList();
        lessons.addAll(newLessons);

        lessons = lessonService.saveOrUpdateAll(lessons);
        schedule.setLessonList(lessons);
        student.setPersonalSchedule(schedule);
        student = studentService.saveOrUpdate(student);
        initializeSchedule(schedule);
        log.info("Updated schedule day '{}' for student '{}' with {} lessons", dayOfWeek, username, lessons.size());
        return scheduleMapper.map(student.getPersonalSchedule());
    }


    /**
     * Initializes lazy-loaded associations of a schedule.
     */
    private void initializeSchedule(Schedule schedule) {
        if (schedule != null && schedule.getLessonList() != null) {
            // Force loading of the lesson list
            schedule.getLessonList().size();
            initializeLessons(schedule.getLessonList());
        }
    }

    /**
     * Iterates over a list of lessons and forces initialization of each.
     */
    private void initializeLessons(List<Lesson> lessons) {
        if (lessons != null) {
            lessons.forEach(this::initializeLesson);
        }
    }

    /**
     * Forces initialization of lazy associations for a lesson.
     */
    private void initializeLesson(Lesson lesson) {
        if (lesson == null) return;
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
            lesson.getGroups().forEach(group -> {
                group.getId();
                if (group.getUniversity() != null) {
                    group.getUniversity().getShortName();
                }
            });
        }
    }

    /**
     * Checks whether a schedule already has a lesson for the given day.
     */
    private void checkExistsScheduleDay(Schedule schedule, List<ScheduleItem> scheduleItems) {
        DayOfWeek day = mapRuDayOfWeek(scheduleItems.get(0).getDayOfWeek());
        schedule.getLessonList().forEach(lesson -> {
            if (lesson.getDayOfWeek().equals(day)) {
                throw new RuntimeException(new ResourceExistsException("Schedule day already exists"));
            }
        });
    }

    /**
     * Maps a Russian day of week string to a DayOfWeek enum.
     */
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

    private Lesson cascadeSaveLesson(Lesson lesson) {
        lesson.setTimeSheet(timeSheetService.findByTimeFromAndTimeTo(lesson.getTimeSheet().getFromTime(), lesson.getTimeSheet().getToTime()));
        lesson.setTeacher(cascadeSaveTeacher(lesson.getTeacher()));
        lesson.setGroups(lesson.getGroups().stream().map(this::cascadeSaveGroup).toList());
        lesson.setPlace(cascadeSavePlace(lesson.getPlace()));

        return lesson;
    }

    private Teacher cascadeSaveTeacher(Teacher teacher) {
        String universityShortName = teacher.getUniversity().getShortName();
        University university = universityService.findByShortName(universityShortName);
        Teacher foundedTeacher = teacherService
                .findByUniversityAndTeacherNameAndLastNameAndFirstNameAndMiddleName(university, teacher.getTeacherName(),
                        teacher.getLastName(), teacher.getFirstName(), teacher.getMiddleName());
        if (foundedTeacher != null) {
           return foundedTeacher;
        } else {
            teacher.setUniversity(university);
            teacher = teacherService.saveOrUpdate(teacher);
        }
        return teacher;
    }

    private Place cascadeSavePlace(Place place) {
        String universityShortName = place.getUniversity().getShortName();
        University university = universityService.findByShortName(universityShortName);
        Place foundedPlace = placeService.findByNameAndUniversity(place.getPlaceName(), university);
        if(foundedPlace != null) {
            return foundedPlace;
        } else {
            place.setUniversity(university);
            place = placeService.saveOrUpdate(place);
        }
        return place;
    }

    private Group cascadeSaveGroup(Group group) {
        String universityShortName = group.getUniversity().getShortName();
        University university = universityService.findByShortName(universityShortName);
        Group foundedGroup = groupService.findByUniversityAndGroupCode(group.getGroupCode(), group.getUniversity());

        if (foundedGroup != null) {
            return foundedGroup;
        } else {
            group.setUniversity(university);
            group = groupService.saveOrUpdate(group);
        }
        return group;
    }
}

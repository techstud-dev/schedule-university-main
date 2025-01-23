package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDayDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dao.entity.Student;
import com.techstud.scheduleuniversity.dao.entity.UniversityGroup;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.ParserResponseTimeoutException;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;
import com.techstud.scheduleuniversity.exception.StudentNotFoundException;
import com.techstud.scheduleuniversity.kafka.KafkaMessageObserver;
import com.techstud.scheduleuniversity.kafka.KafkaProducer;
import com.techstud.scheduleuniversity.repository.jpa.StudentRepository;
import com.techstud.scheduleuniversity.repository.jpa.UniversityGroupRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleDayRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleRepositoryFacade;
import com.techstud.scheduleuniversity.repository.mongo.TimeSheetRepository;
import com.techstud.scheduleuniversity.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UniversityGroupRepository universityGroupRepository;
    private final ScheduleRepositoryFacade scheduleRepositoryFacade;
    private final KafkaProducer kafkaProducer;
    private final KafkaMessageObserver messageObserver;
    private final StudentRepository studentRepository;
    private final ScheduleDayRepository scheduleDayRepository;
    private final TimeSheetRepository timeSheetRepository;

    @Override
    @Transactional
    public ScheduleDocument importSchedule(ImportDto importDto, String username) throws ScheduleNotFoundException, ParserException {
        log.info("Importing schedule for university: {}, group: {}", importDto.getUniversityName(), importDto.getGroupCode());

        ScheduleDocument scheduleDocument = null;

        Student student = studentRepository.findByUsername(username)
                .orElseGet(() -> studentRepository.save(new Student(username)));

        var group = universityGroupRepository
                .findByUniversityShortNameAndGroupCode(importDto.getUniversityName(), importDto.getGroupCode())
                .orElseThrow(() -> new IllegalArgumentException("Group " + importDto.getGroupCode() + " not found"));

        if (group.getScheduleMongoId() != null) {
            scheduleDocument = scheduleRepository.findById(group.getScheduleMongoId())
                    .orElse(null);
        }

        if (scheduleDocument == null && student.getScheduleMongoId() != null) {
            scheduleDocument = scheduleRepository.findById(student.getScheduleMongoId())
                    .orElse(null);
        }

        if (scheduleDocument != null) {
            return scheduleDocument;
        }

        log.warn("Schedule not found for group {}. Attempting to import from parser.", importDto.getGroupCode());
        scheduleDocument = fullFetchAndSaveSchedule(group, student);
        if (scheduleDocument == null) {
            throw new ScheduleNotFoundException("Schedule not found for group " + importDto.getGroupCode());
        }
        return scheduleDocument;
    }

    @Override
    @Transactional
    public ScheduleDocument createSchedule(ScheduleParserResponse saveDto, String username) {
        log.info("Saving schedule for user: {}", username);

        ScheduleDocument savedDocument;

        var student = studentRepository
                .findByUsername(username)
                .orElseGet(() -> studentRepository.save(new Student(username)));

        if (student.getScheduleMongoId() != null) {
            scheduleRepository.deleteById(student.getScheduleMongoId());
        }

        savedDocument = scheduleRepositoryFacade.cascadeSave(saveDto);

        student.setLastAction(LocalDate.now());
        student.setScheduleMongoId(savedDocument.getId());

        studentRepository.save(student);

        return savedDocument;
    }

    @Override
    public ScheduleDocument forceImportSchedule(ImportDto importDto, String username) throws ParserException, ScheduleNotFoundException {

        var student = studentRepository.findByUsername(username)
                .orElseGet(() -> studentRepository.save(new Student(username)));

        var group = universityGroupRepository
                .findByUniversityShortNameAndGroupCode(importDto.getUniversityName(), importDto.getGroupCode())
                .orElseThrow(() -> new IllegalArgumentException("Group " + importDto.getGroupCode() + " not found"));

        var scheduleDocument = fetchAndSaveSchedule(group, student);
        if (scheduleDocument == null) {
            throw new ScheduleNotFoundException("Schedule not found for group " + importDto.getGroupCode());
        }
        return scheduleDocument;
    }

    @Override
    @Transactional
    public void deleteSchedule(String scheduleId, String username) throws ScheduleNotFoundException, StudentNotFoundException {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(()-> new StudentNotFoundException("Student not found for username: " + username));

        String scheduleDbId = student.getScheduleMongoId();

        if (!scheduleDbId.equals(scheduleId)) {
            throw new ScheduleNotFoundException("Schedule scheduleId does not match student's schedule scheduleId");
        }

        scheduleRepository.deleteById(scheduleId);
        student.setScheduleMongoId(null);
        studentRepository.save(student);
    }

    @Override
    @Transactional
    public ScheduleDocument deleteScheduleDay(String dayId, String username) throws ScheduleNotFoundException, StudentNotFoundException {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(()-> new StudentNotFoundException("Student not found for username: " + username));

        String scheduleId = student.getScheduleMongoId();

        ScheduleDocument schedule = getScheduleById(scheduleId);

        ScheduleDayDocument scheduleDay = scheduleDayRepository.findById(dayId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule day not found for id: " + dayId));

        return scheduleRepositoryFacade.smartScheduleDayDelete(schedule, scheduleDay);
    }

    @Override
    @Transactional
    public ScheduleDocument deleteLesson(String scheduleDayId, String timeWindowId, String username) throws ScheduleNotFoundException, StudentNotFoundException {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(()-> new StudentNotFoundException("Student not found for username: " + username));


        ScheduleDocument schedule = getScheduleById(student.getScheduleMongoId());

        return scheduleRepositoryFacade.smartLessonDelete(schedule, scheduleDayId, timeWindowId);
    }

    @Override
    @Transactional
    public ScheduleDocument getScheduleById(String scheduleId) throws ScheduleNotFoundException {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found for id: " + scheduleId));
    }

    @Override
    @Transactional
    public ScheduleDocument getScheduleByStudentName(String studentName) throws ScheduleNotFoundException, StudentNotFoundException {
        Student student = studentRepository.findByUsername(studentName)
                .orElseThrow(()-> new StudentNotFoundException("Student not found for username: " + studentName));

        return scheduleRepository.findById(student.getScheduleMongoId())
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found for student: " + studentName));
    }

    @Override
    @Transactional
    public ScheduleDocument updateLesson(String scheduleDayId, String timeWindowId, ScheduleItem scheduleItem, String username) throws ScheduleNotFoundException, StudentNotFoundException {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(()-> new StudentNotFoundException("Student not found for username: " + username));

        ScheduleDocument schedule = getScheduleById(student.getScheduleMongoId());

        schedule = scheduleRepositoryFacade.smartLessonUpdate(schedule, scheduleDayId, timeWindowId, scheduleItem);
        student.setScheduleMongoId(schedule.getId());
        studentRepository.save(student);

        return schedule;
    }

    @Override
    @Transactional
    public ScheduleDocument updateScheduleDay(String dayId, List<ScheduleItem> scheduleItems, String username) throws ScheduleNotFoundException, StudentNotFoundException {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(()-> new StudentNotFoundException("Student not found for username: " + username));

        ScheduleDocument schedule = getScheduleById(student.getScheduleMongoId());

        schedule = scheduleRepositoryFacade.smartScheduleDayUpdate(schedule, dayId, scheduleItems);
        student.setScheduleMongoId(schedule.getId());
        studentRepository.save(student);

        return schedule;
    }

    private ScheduleDocument fetchAndSaveSchedule(UniversityGroup group, Student student) throws ParserException {
        ScheduleDocument savedSchedule = null;
        ParsingTask parsingTask = ParsingTask.builder()
                .groupId(group.getUniversityGroupId())
                .universityName(group.getUniversity().getShortName())
                .build();

        UUID uuid = kafkaProducer.sendToParsingQueue(parsingTask);

        try {
            var parserSchedule = messageObserver.waitForParserResponse(uuid);
            if (parserSchedule != null) {
                savedSchedule = scheduleRepositoryFacade.cascadeSave(parserSchedule);
                scheduleRepository.deleteById(student.getScheduleMongoId());
                student.setScheduleMongoId(savedSchedule.getId());
                studentRepository.save(student);
            }
        } catch (ParserResponseTimeoutException  e) {
            log.error("Error while waiting for parser response", e);
        }
        return savedSchedule;
    }

    private ScheduleDocument fullFetchAndSaveSchedule(UniversityGroup group, Student student) throws ParserException {
        ScheduleDocument savedSchedule = null;
        ParsingTask parsingTask = ParsingTask.builder()
                .groupId(group.getUniversityGroupId())
                .universityName(group.getUniversity().getShortName())
                .build();

        UUID uuid = kafkaProducer.sendToParsingQueue(parsingTask);

        try {
            var parserSchedule = messageObserver.waitForParserResponse(uuid);
            if (parserSchedule != null) {
                savedSchedule = scheduleRepositoryFacade.cascadeSave(parserSchedule);
                if (student.getScheduleMongoId() != null) {
                    scheduleRepository.deleteById(student.getScheduleMongoId());
                }
                student.setScheduleMongoId(savedSchedule.getId());
                studentRepository.save(student);
            }
        } catch (ParserResponseTimeoutException e) {
            log.error("Error while waiting for parser response", e);
        }
        return savedSchedule;
    }

    @Override
    @Transactional
    public ScheduleDocument saveLessons(List<ScheduleItem> items, String userName) throws StudentNotFoundException, ScheduleNotFoundException {
        Student student = studentRepository.findByUsername(userName)
                .orElseThrow(() -> new StudentNotFoundException("Not found student by username, %s ".formatted(userName)));

        ScheduleDocument schedule = scheduleRepository.findById(student.getScheduleMongoId())
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule by id, %s, not found".formatted(student.getScheduleMongoId())));

        var isEvenWeek = items.stream().map(ScheduleItem::isEven).findFirst();

        var dayOfWeek = Instant.ofEpochMilli(items.stream()
                        .map(ScheduleItem::getDate)
                        .findFirst()
                        .get())
                .atZone(ZoneId.systemDefault()).getDayOfWeek();

        Map<DayOfWeek, ScheduleDayDocument> week;

        if (isEvenWeek.isPresent()) {
            week = schedule.getEvenWeekSchedule();
        } else {
            week = schedule.getOddWeekSchedule();
        }

        var scheduleDay = week.entrySet().stream()
                .filter(key -> key.getKey().equals(dayOfWeek))
                .map(Map.Entry::getValue)
                .findFirst().orElseThrow(() -> new IllegalStateException("Schedule day not found"));

        var foundedTimeSheet = scheduleDay.getLessons().keySet().stream()
                .map(key -> timeSheetRepository.findById(key).get())
                .toList();

        var isLessonsInDay = items
                .stream()
                .anyMatch(item -> {
                    var timeInfo = item.getTime();
                    var from = LocalTime.parse(timeInfo.split("-")[0]);
                    var to = LocalTime.parse(timeInfo.split("-")[1]);
                    return foundedTimeSheet.stream()
                            .anyMatch(timeSheetDocument ->
                                    timeSheetDocument.getTo().equals(to) &&
                                            timeSheetDocument.getFrom().equals(from)
                            );
                });

        if(!isLessonsInDay){
            var lessons = scheduleRepositoryFacade.convertItemToLessons(items);

            lessons.forEach((day, newLessons) ->
                    scheduleDay.getLessons()
                            .merge(day, new ArrayList<>(newLessons),
                            (existingLessons, lessonsToAdd) -> {
                                existingLessons.addAll(lessonsToAdd);
                                return existingLessons;
                            }));

            scheduleRepositoryFacade.computeAndSetHash(scheduleDay);
            scheduleDayRepository.save(scheduleDay);
            log.info("Successful save day with lessons, {}", scheduleDay);

            if(isEvenWeek.isPresent()){
                schedule.getEvenWeekSchedule().put(dayOfWeek, scheduleDay);
            } else {
                schedule.getOddWeekSchedule().put(dayOfWeek, scheduleDay);
            }
        } else {
            throw new IllegalStateException("Time sheet object, %s, already exists in day: ".formatted(dayOfWeek));
        }

        scheduleRepositoryFacade.computeAndSetHash(schedule);
        return scheduleRepository.save(schedule);
    }
}

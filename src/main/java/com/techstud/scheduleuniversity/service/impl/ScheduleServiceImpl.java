package com.techstud.scheduleuniversity.service.impl;


import com.jayway.jsonpath.Criteria;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDayDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleObjectDocument;
import com.techstud.scheduleuniversity.dao.entity.Student;
import com.techstud.scheduleuniversity.dao.entity.UniversityGroup;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleDayParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.ParserResponseTimeoutException;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;
import com.techstud.scheduleuniversity.kafka.KafkaMessageObserver;
import com.techstud.scheduleuniversity.kafka.KafkaProducer;
import com.techstud.scheduleuniversity.mapper.ScheduleDayMapper;
import com.techstud.scheduleuniversity.repository.jpa.StudentRepository;
import com.techstud.scheduleuniversity.repository.jpa.UniversityGroupRepository;
import com.techstud.scheduleuniversity.repository.mongo.*;
import com.techstud.scheduleuniversity.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final ScheduleDayMapper scheduleDayMapper;
    private final ScheduleObjectRepository scheduleObjectRepository;
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

        if (student.getScheduleMongoId() != null) {
            scheduleDocument = scheduleRepository.findById(student.getScheduleMongoId())
                    .orElse(null);
        }

        if (scheduleDocument != null) {
            log.info("Schedule found for group {}. Returning existing schedule.", importDto.getGroupCode());
            return scheduleDocument;
        }

        log.warn("Schedule not found for group {}. Attempting to import from parser.", importDto.getGroupCode());
        scheduleDocument  = fetchAndSaveSchedule(group, student);
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

    private ScheduleDocument fetchAndSaveSchedule(UniversityGroup group, Student student) throws ParserException {
        ScheduleDocument savedSchedule = null;
        ParsingTask parsingTask = ParsingTask.builder()
                .groupId(group.getUniversityGroupId())
                .universityName(group.getUniversity().getShortName())
                .build();

        UUID uuid = kafkaProducer.sendToParsingQueue(parsingTask);

        try {
            var parserSchedule = messageObserver.waitForParserResponse(uuid);
            if(parserSchedule != null) {
                savedSchedule = scheduleRepositoryFacade.cascadeSave(parserSchedule);
                student.setScheduleMongoId(savedSchedule.getId());
                studentRepository.save(student);
            }
        } catch (ParserResponseTimeoutException  e) {
            log.error("Error while waiting for parser response", e);
        }
        return savedSchedule;
    }

    @Override
    @Transactional
    public ScheduleDayDocument saveScheduleDay(final ScheduleDayParserResponse saveDayResponse, final String scheduleDayId){
        return scheduleDayRepository.findById(scheduleDayId).map(
                        existingDay ->{
                            existingDay.getLessons().values().stream()
                                    .filter(Objects::nonNull)
                                    .flatMap(Collection::stream)
                                    .map(ScheduleObjectDocument::getId)
                                    .filter(Objects::nonNull)
                                    .forEach(scheduleObjectRepository::deleteById);
                            log.info("Successfully delete schedule object {}.", LocalDateTime.now());

                            final ScheduleDayDocument updatedDay = ScheduleDayDocument.builder()
                                    .id(existingDay.getId())
                                    .date(saveDayResponse.getDate())
                                    .lessons(scheduleRepositoryFacade.cascadeLessonSave(saveDayResponse.getLessons()))
                                    .hash(existingDay.getHash())
                                    .build();
                            log.info("Successfully updated schedule day {}.", existingDay.getHash());
                            return scheduleDayRepository.save(updatedDay);
                        })
                .orElseGet(() -> {
                    log.info("Successfully saved schedule day with date: {}", saveDayResponse.getDate());
                    return scheduleRepositoryFacade.cascadeDaySave(saveDayResponse);
                });
    }
}


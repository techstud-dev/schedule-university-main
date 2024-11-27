package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dao.document.schedule.Schedule;
import com.techstud.scheduleuniversity.dao.entity.Student;
import com.techstud.scheduleuniversity.dao.entity.UniversityGroup;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.ParserResponseTimeoutException;
import com.techstud.scheduleuniversity.kafka.KafkaMessageObserver;
import com.techstud.scheduleuniversity.kafka.KafkaProducer;
import com.techstud.scheduleuniversity.mapper.ScheduleMapper;
import com.techstud.scheduleuniversity.repository.jpa.StudentRepository;
import com.techstud.scheduleuniversity.repository.jpa.UniversityGroupRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleRepositoryFacade;
import com.techstud.scheduleuniversity.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    @Override
    @Transactional
    public Schedule importSchedule(ImportDto importDto, String username) {
        log.info("Importing schedule for university: {}, group: {}", importDto.getUniversityName(), importDto.getGroupCode());

        Student student = studentRepository.findByUsername(username)
                .orElseGet(() -> studentRepository.save(new Student(username)));

        var group = universityGroupRepository
                .findByUniversityShortNameAndGroupCode(importDto.getUniversityName(), importDto.getGroupCode())
                .orElseThrow(() -> new IllegalArgumentException("Group " + importDto.getGroupCode() + " not found"));

        if (student.getScheduleMongoId() != null) {
            return scheduleRepository.findById(student.getScheduleMongoId())
                    .orElseThrow(() -> new IllegalArgumentException("Schedule not found for student"));
        }

        if (group.getScheduleMongoId() != null) {

            student.setScheduleMongoId(group.getScheduleMongoId());
            studentRepository.save(student);

            return scheduleRepository.findById(group.getScheduleMongoId())
                    .orElseThrow(() -> new IllegalArgumentException("Schedule not found for group"));
        }

        log.warn("Schedule not found for group {}. Attempting to import from parser.", importDto.getGroupCode());
        return fetchAndSaveSchedule(group, student);
    }


    private Schedule fetchAndSaveSchedule(UniversityGroup group, Student student) {
        ParsingTask parsingTask = ParsingTask.builder()
                .groupId(group.getUniversityGroupId())
                .universityName(group.getUniversity().getShortName())
                .build();

        UUID uuid = kafkaProducer.sendToParsingQueue(parsingTask);

        try {
            var parserSchedule = messageObserver.waitForParserResponse(uuid);
            Schedule savedSchedule = scheduleRepositoryFacade.cascadeSave(parserSchedule);
            student.setScheduleMongoId(savedSchedule.getId());
            studentRepository.save(student);
            return savedSchedule;
        } catch (ParserResponseTimeoutException | ParserException e) {
            log.error("Error while waiting for parser response", e);
            throw new RuntimeException("Failed to import schedule", e);
        }
    }
}

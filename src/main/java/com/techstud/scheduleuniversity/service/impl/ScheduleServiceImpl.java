package com.techstud.scheduleuniversity.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techstud.scheduleuniversity.dao.document.Schedule;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.ParserResponseTimeoutException;
import com.techstud.scheduleuniversity.kafka.KafkaMessageObserver;
import com.techstud.scheduleuniversity.kafka.KafkaProducer;
import com.techstud.scheduleuniversity.repository.jpa.UniversityGroupRepository;
import com.techstud.scheduleuniversity.repository.mongo.*;
import com.techstud.scheduleuniversity.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UniversityGroupRepository universityGroupRepository;
    private final ScheduleDayRepository scheduleDayRepository;
    private final TimeSheetRepository timeSheetRepository;
    private final ScheduleObjectRepository scheduleObjectRepository;
    private final ScheduleRepositoryFacade scheduleRepositoryFacade;
    private final KafkaProducer kafkaProducer;
    private final KafkaMessageObserver messageObserver;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Schedule importSchedule(ImportDto importDto) {
        log.info("Import schedule for university: {}, group: {}", importDto.getUniversityName(), importDto.getGroupCode());
        Schedule schedule = null;
        var group = universityGroupRepository
                .findByUniversityShortNameAndGroupCode(importDto.getUniversityName(), importDto.getGroupCode())
                .orElseThrow(() -> new IllegalArgumentException("Group "+ importDto.getGroupCode() +" not found"));

        if (group.getScheduleMongoId() != null) {
            schedule = scheduleRepository
                    .findById(group.getScheduleMongoId())
                    .orElse(null);
        }

        if (schedule == null) {
            log.warn("Not found schedule for group {}. Trying to import schedule from parser", importDto.getGroupCode());
            ParsingTask parsingTask = ParsingTask.builder()
                    .groupId(group.getUniversityGroupId())
                    .universityName(importDto.getUniversityName())
                    .build();
            UUID uuid = kafkaProducer.sendToParsingQueue(parsingTask);
            try {
                schedule = scheduleRepositoryFacade.cascadeSave(messageObserver.waitForParserResponse(uuid));
                if (schedule != null) {
                    group.setScheduleMongoId(schedule.getId());
                    universityGroupRepository.save(group);
                }
            } catch (ParserResponseTimeoutException | ParserException | NoSuchAlgorithmException |
                     JsonProcessingException exception) {
                log.error("Error waiting response from schedule", exception);
            }
        }
        return schedule;
    }
}

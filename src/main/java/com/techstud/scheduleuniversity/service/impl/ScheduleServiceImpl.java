package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dao.document.Schedule;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.ParserResponseTimeoutException;
import com.techstud.scheduleuniversity.kafka.KafkaMessageObserver;
import com.techstud.scheduleuniversity.kafka.KafkaProducer;
import com.techstud.scheduleuniversity.mapper.ScheduleMapper;
import com.techstud.scheduleuniversity.repository.jpa.UniversityGroupRepository;
import com.techstud.scheduleuniversity.repository.mongo.*;
import com.techstud.scheduleuniversity.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    private final ScheduleMapper scheduleMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public com.techstud.scheduleuniversity.dto.response.schedule.Schedule importSchedule(ImportDto importDto) {
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
        if (schedule != null) {
            return scheduleMapper.toResponse(schedule);
        }
        Schedule documentSchedule;
        com.techstud.scheduleuniversity.dto.parser.response.Schedule parserSchedule;
        com.techstud.scheduleuniversity.dto.response.schedule.Schedule responseSchedule = null;

            log.warn("Not found schedule for group {}. Trying to import schedule from parser", importDto.getGroupCode());
            ParsingTask parsingTask = ParsingTask.builder()
                    .groupId(group.getUniversityGroupId())
                    .universityName(importDto.getUniversityName())
                    .build();
            UUID uuid = kafkaProducer.sendToParsingQueue(parsingTask);
            try {
                parserSchedule = messageObserver.waitForParserResponse(uuid);
                documentSchedule = scheduleRepositoryFacade.cascadeSave(parserSchedule);
                if (documentSchedule != null) {
                    group.setScheduleMongoId(documentSchedule.getId());
                    universityGroupRepository.save(group);
                    responseSchedule = scheduleMapper.toResponse(documentSchedule);
                }
            } catch (ParserResponseTimeoutException | ParserException exception) {
                log.error("Error waiting response from schedule", exception);
            }

        return responseSchedule;
    }
}

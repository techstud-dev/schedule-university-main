package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dao.document.Schedule;
import com.techstud.scheduleuniversity.dao.entity.UniversityGroup;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.exception.ParserResponseTimeoutException;
import com.techstud.scheduleuniversity.kafka.KafkaProducer;
import com.techstud.scheduleuniversity.repository.jpa.UniversityGroupRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleDayRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleObjectRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleRepository;
import com.techstud.scheduleuniversity.repository.mongo.TimeSheetRepository;
import com.techstud.scheduleuniversity.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.techstud.scheduleuniversity.kafka.KafkaMessageObserver.waitForParserResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UniversityGroupRepository universityGroupRepository;
    private final ScheduleDayRepository scheduleDayRepository;
    private final TimeSheetRepository timeSheetRepository;
    private final ScheduleObjectRepository scheduleObjectRepository;
    private final KafkaProducer kafkaProducer;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Schedule importSchedule(ImportDto importDto) {
        Schedule schedule = null;
        var group = universityGroupRepository.findByUniversity_ShortNameAndGroupCode(importDto.getGroupCode(),
                importDto.getUniversityName());

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
                schedule = waitForParserResponse(uuid);
            } catch (ParserResponseTimeoutException exception) {
                log.error("Error waiting response from schedule", exception);
            }
            if (schedule != null) {
                Schedule scheduleSaved = scheduleRepository.save(schedule);
                group.setScheduleMongoId(scheduleSaved.getId());
                universityGroupRepository.save(group);
            }
        }
        return schedule;
    }
}

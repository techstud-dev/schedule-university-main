package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.entity.Schedule;
import com.techstud.scheduleuniversity.entity.Student;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.ParserResponseTimeoutException;
import com.techstud.scheduleuniversity.kafka.KafkaMessageObserver;
import com.techstud.scheduleuniversity.kafka.KafkaProducer;
import com.techstud.scheduleuniversity.service.ParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParserServiceImpl implements ParserService {

    private final KafkaProducer kafkaProducer;
    private final KafkaMessageObserver kafkaMessageObserver;

    @Override
    public ScheduleParserResponse parseSchedule(ImportDto parsingTask, Student student) throws ParserException, ParserResponseTimeoutException {
        UUID taskId = kafkaProducer.sendToParsingQueue(parsingTask);
        kafkaMessageObserver.registerMessage(taskId);
        ScheduleParserResponse response = kafkaMessageObserver.waitForParserResponse(taskId);
        log.info("Parsed schedule with id = {}, response = {}", taskId, response);
        return response;
    }
}

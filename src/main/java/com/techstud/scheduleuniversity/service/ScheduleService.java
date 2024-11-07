package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.entity.schedule.Schedule;
import com.techstud.scheduleuniversity.kafka.KafkaProducer;
import com.techstud.scheduleuniversity.listener.KafkaResponseHandler;
import com.techstud.scheduleuniversity.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static java.util.UUID.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final KafkaProducer kafkaProducer;
    private final KafkaResponseHandler responseHandler;

    @Transactional(propagation = Propagation.REQUIRED)
    public Mono<Schedule> getScheduleByGroupName(String groupName) {
        return scheduleRepository.findByGroupName(groupName);
    }

    public Mono<com.techstud.scheduleuniversity.dto.parser.response.Schedule> importSchedule(ParsingTask task) {
        String id = randomUUID().toString();
        log.info("Importing schedule with task:\n{}", task);
        Mono<com.techstud.scheduleuniversity.dto.parser.response.Schedule> response =  Mono.<com.techstud.scheduleuniversity.dto.parser.response.Schedule>create(sink -> {
                    responseHandler.register(id, sink);
                    kafkaProducer.sendToParsingQueue(id,task);
                }).timeout(Duration.ofSeconds(30))
                .doOnError(TimeoutException.class, ex -> responseHandler.remove(id));
        log.info("Success importing schedule! Response: {}", response);
        return response;
    }
}

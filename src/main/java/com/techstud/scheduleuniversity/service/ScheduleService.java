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
import java.util.concurrent.TimeoutException;

import static java.util.UUID.randomUUID;

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
        return Mono
                .<com.techstud.scheduleuniversity.dto.parser.response.Schedule>create(sink -> {
                    responseHandler.register(id, sink);
                    kafkaProducer.sendToParsingQueue(id,task);
                })
                .timeout(Duration.ofSeconds(30))
                .doOnError(TimeoutException.class, ex -> responseHandler.remove(id))
                .single()
                .doOnSubscribe(subscription -> log.info("Import schedule task with task: {} started", task))
                .doOnNext(schedule -> log.info("Import schedule complete {}", schedule))
                .doOnError(throwable -> log.error("Import schedule task with task: {} failed", task, throwable));
    }
}

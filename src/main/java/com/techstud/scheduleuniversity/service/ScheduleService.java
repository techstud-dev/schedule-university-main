package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.entity.schedule.Schedule;
import com.techstud.scheduleuniversity.entity.schedule.University;
import com.techstud.scheduleuniversity.kafka.KafkaProducer;
import com.techstud.scheduleuniversity.listener.KafkaResponseHandler;
import com.techstud.scheduleuniversity.mapper.ScheduleMapper;
import com.techstud.scheduleuniversity.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleService {

    private final UniversityRepository universityRepository;
    private final KafkaProducer kafkaProducer;
    private final KafkaResponseHandler responseHandler;
    private final ScheduleMapper mapper;
    private final TransactionExecutor transactionExecutor;


    public Mono<Schedule> importSchedule(ParsingTask task) {
        String id = UUID.randomUUID().toString();
        Mono<University> universityMono = Mono.justOrEmpty(universityRepository.findByName(task.getUniversityName()));

        Mono<com.techstud.scheduleuniversity.dto.parser.response.Schedule> responseMono = Mono
                .<com.techstud.scheduleuniversity.dto.parser.response.Schedule>create(sink -> {
                    responseHandler.register(id, sink);
                    kafkaProducer.sendToParsingQueue(id, task);
                })
                .timeout(Duration.ofSeconds(30))
                .doOnError(TimeoutException.class, ex -> responseHandler.remove(id))
                .doOnSubscribe(subscription -> log.info("Import schedule task with task: {} started", task))
                .doOnNext(schedule -> log.info("Import schedule complete {}", schedule))
                .doOnError(throwable -> log.error("Import schedule task with task: {} failed", task, throwable));

        return universityMono
                .switchIfEmpty(Mono.error(new IllegalArgumentException("University not found")))
                .flatMap(university ->
                        responseMono.flatMap(dto -> {
                            Schedule schedule;
                            try {
                                schedule = mapper.mapDtoToEntity(dto, university);
                            } catch (Exception e) {
                                return Mono.error(e);
                            }
                            return transactionExecutor.smartScheduleSave(schedule);
                        })
                );
    }
}

package com.techstud.scheduleuniversity.kafka;

import com.techstud.scheduleuniversity.dao.document.Schedule;
import com.techstud.scheduleuniversity.exception.ParserResponseTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("singleton")
@Slf4j
public class KafkaMessageObserver {

    private static final Set<String> messageInProcessing = new HashSet<>();

    private static final Map<String, Object> responsesFromParser = new HashMap<>();

    @Async
    public static boolean registerMessage(UUID uuid) {

        if (messageInProcessing.contains(uuid.toString())) {
            log.warn("Message with id = {} is registered in observer", uuid);
            return false;
        }

        messageInProcessing.add(uuid.toString());
        return true;
    }

    @Async
    public static Schedule waitForParserResponse(UUID uuid) throws ParserResponseTimeoutException {
        Long startTime = System.nanoTime() / 1000000;
        if (!messageInProcessing.contains(uuid.toString())) {
            log.error("Message with id = {} not sent into kafka or not registered", uuid);
            return null;
        }
        while (!responsesFromParser.containsKey(uuid.toString())) {
            Long currentTime = System.nanoTime() / 1000000;
            if (currentTime - startTime >= 10000) {
                throw new ParserResponseTimeoutException(uuid);
            }
        }
        Long endTime = System.nanoTime() / 1000000;
        log.info("End waiting response from parser. Time spent {} ms", endTime - startTime);
        Object response = responsesFromParser.get(uuid.toString());
        if (response instanceof Schedule) {
            return (Schedule) responsesFromParser.get(uuid.toString());
        } else {
            throw new ParserException();
        }
    }
}

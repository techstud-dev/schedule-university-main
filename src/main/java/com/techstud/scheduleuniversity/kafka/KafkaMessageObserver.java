package com.techstud.scheduleuniversity.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.exception.ParserException;
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
    private static final Map<String, String> responsesFromParser = new HashMap<>();

    ObjectMapper objectMapper = new ObjectMapper();

    @Async
    public boolean registerMessage(UUID uuid) {

        if (messageInProcessing.contains(uuid.toString())) {
            log.warn("Message with id = {} is registered in observer", uuid);
            return false;
        }

        messageInProcessing.add(uuid.toString());
        return true;
    }

    @Async
    public void registerResponse(UUID uuid, String response) {
        responsesFromParser.put(uuid.toString(), response);
    }

    @Async
    public ScheduleParserResponse waitForParserResponse(UUID uuid) throws ParserResponseTimeoutException, ParserException {
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
        try {
            return objectMapper.readValue(responsesFromParser.get(uuid.toString()), ScheduleParserResponse.class);
        } catch (JsonProcessingException exception) {
            throw new ParserException("Error parsing response from parser", exception);
        } finally {
            messageInProcessing.remove(uuid.toString());
            responsesFromParser.remove(uuid.toString());
        }
    }
}

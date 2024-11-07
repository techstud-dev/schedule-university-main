package com.techstud.scheduleuniversity.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstud.scheduleuniversity.dto.parser.response.Schedule;
import com.techstud.scheduleuniversity.exception.ParsingException;
import com.techstud.scheduleuniversity.listener.KafkaResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaResponseHandler responseHandler;

    @KafkaListener(topics = "#{'${kafka.topic.parsing-result}'}", concurrency = "${spring.kafka.listener.concurrency}",
            autoStartup = "true", groupId = "parser_group")
    public void listenParsingResult(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        String id = consumerRecord.key();
        String message = consumerRecord.value();
        log.info("Received message with id: {}, message:\n{}", id, message);
        Schedule schedule = objectMapper.readValue(message, Schedule.class);
        responseHandler.completeSuccess(id, schedule);
    }

    @KafkaListener(topics = "#{'${kafka.topic.parsing-failure}'}", concurrency = "${spring.kafka.listener.concurrency}",
            autoStartup = "true", groupId = "parser_group")
    public void listenParsingFailure(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        String id = consumerRecord.key();
        String message = consumerRecord.value();
        Map messageMap = objectMapper.readValue(message, Map.class);
        responseHandler.completeError(id, new ParsingException(messageMap));
    }
}

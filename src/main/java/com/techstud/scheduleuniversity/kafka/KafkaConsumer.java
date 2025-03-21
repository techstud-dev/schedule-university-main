package com.techstud.scheduleuniversity.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaMessageObserver messageObserver;

    @KafkaListener(topics = "#{'${kafka.topic.parsing-result}'}", concurrency = "${spring.kafka.listener.concurrency}",
            autoStartup = "true", groupId = "parser_group")
    public void listenParsingResult(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        String id = consumerRecord.key();
        String message = consumerRecord.value();
        log.info("Received message with id: {}, message:\n{}", id, message);
        messageObserver.registerResponse(UUID.fromString(id), message);
    }

    @KafkaListener(topics = "#{'${kafka.topic.parsing-failure}'}", concurrency = "${spring.kafka.listener.concurrency}",
            autoStartup = "true", groupId = "parser_group")
    public void listenParsingFailure(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        String id = consumerRecord.key();
        String message = consumerRecord.value();
        log.warn("Received failure message with id: {}, message:\n{}", id, message);
        messageObserver.registerResponse(UUID.fromString(id), message);
    }
}

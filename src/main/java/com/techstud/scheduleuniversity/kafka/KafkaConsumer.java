package com.techstud.scheduleuniversity.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "#{'${kafka.topic.parsing-result}'}", concurrency = "${spring.kafka.listener.concurrency}",
            autoStartup = "true", groupId = "parser_group")
    public void listenParsingResult(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {

    }

    @KafkaListener(topics = "#{'${kafka.topic.parsing-failure}'}", concurrency = "${spring.kafka.listener.concurrency}",
            autoStartup = "true", groupId = "parser_group")
    public void listenParsingFailure(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {

    }
}

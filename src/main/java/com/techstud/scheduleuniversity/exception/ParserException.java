package com.techstud.scheduleuniversity.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.StandardException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@StandardException
@Slf4j
public class ParserException extends Exception {
    ObjectMapper mapper = new ObjectMapper();
    public ParserException(String parserFailureResponse) {
        try {
            Map parserResponse = mapper.readValue(parserFailureResponse, Map.class);
        } catch (JsonProcessingException exception) {
            log.error("Error while parsing parser response", exception);
        }
    }
}

package com.techstud.scheduleuniversity.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.experimental.StandardException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Getter
@StandardException
@Slf4j
public class ParserException extends Exception {

    private Map parserResponse;

    public ParserException(String parserFailureResponse) {
        super("Error parsing schedule");
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.parserResponse = mapper.readValue(parserFailureResponse, Map.class);
        } catch (JsonProcessingException exception) {
            log.error("Error while parsing parser response", exception);
        }
    }

}

package com.techstud.scheduleuniversity.exception;

import lombok.experimental.StandardException;

import java.util.UUID;

@StandardException
public class ParserResponseTimeoutException extends Exception {

    private static final String standardMessage = "Timeout waiting response from sch-parser. Id: ";

    public ParserResponseTimeoutException(UUID uuid) {
        super(standardMessage + uuid);
    }
}

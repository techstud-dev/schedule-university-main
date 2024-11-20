package com.techstud.scheduleuniversity.exception;

import lombok.experimental.StandardException;

import java.util.List;

@StandardException
public class RequestException extends Exception {

    private static final String standardMessage = "The required fields are missing in the request. Fields: ";

    public RequestException(List<String> emptyFields) {
        super(standardMessage + emptyFields.toString());
    }

}

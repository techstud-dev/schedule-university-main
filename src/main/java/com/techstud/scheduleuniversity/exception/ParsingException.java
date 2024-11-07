package com.techstud.scheduleuniversity.exception;


import com.google.gson.Gson;
import com.techstud.scheduleuniversity.dto.parser.ErrorResponseDTO;
import lombok.Getter;

import java.util.Map;

@Getter
public class ParsingException extends RuntimeException {

    private final ErrorResponseDTO errorResponse = new ErrorResponseDTO();

    public ParsingException(Map<String, String> errorResponse) {
        super(new Gson().toJson(errorResponse));
        this.errorResponse.setMessage(errorResponse.get("message"));
        this.errorResponse.setServiceName(errorResponse.get("serviceName"));
        this.errorResponse.setSystemName(errorResponse.get("systemName"));
        this.errorResponse.setMessageId(errorResponse.get("messageId"));
    }

}

package com.techstud.scheduleuniversity.dto.parser;

import lombok.Data;

import java.io.Serializable;

@Data
public class ErrorResponseDTO implements Serializable {
    private String systemName;
    private String serviceName;
    private String messageId;
    private String message;
}

package com.techstud.scheduleuniversity.dto.parser.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeSheet implements Serializable {

    private LocalTime from;

    private LocalTime to;

    public TimeSheet(String from, String to) {
        this.from = LocalTime.parse(from);
        this.to = LocalTime.parse(to);
    }

    public TimeSheet(String messageObject) {
        if (messageObject == null || !messageObject.startsWith("TimeSheet(") || !messageObject.endsWith(")")) {
            throw new IllegalArgumentException("Invalid message format");
        }

        String content = messageObject.substring("TimeSheet(".length(), messageObject.length() - 1);

        String[] parts = content.split(",\\s*");

        for (String part : parts) {
            String[] keyValue = part.split("=");
            if (keyValue.length != 2) {
                throw new IllegalArgumentException("Invalid key=value pair in message: " + part);
            }
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            try {
                if ("from".equals(key)) {
                    this.from = LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"));
                } else if ("to".equals(key)) {
                    this.to = LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"));
                } else {
                    throw new IllegalArgumentException("Unknown key in message: " + key);
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid time format for key: " + key, e);
            }
        }

        if (this.from == null || this.to == null) {
            throw new IllegalArgumentException("Both 'from' and 'to' times must be provided");
        }
    }
}

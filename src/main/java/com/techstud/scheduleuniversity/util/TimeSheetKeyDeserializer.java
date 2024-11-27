package com.techstud.scheduleuniversity.util;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.techstud.scheduleuniversity.dto.parser.response.TimeSheetParserResponse;

import java.time.LocalTime;

public class TimeSheetKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) {
        String[] parts = key.replace("TimeSheet(from=", "").replace(")", "").split(", to=");
        TimeSheetParserResponse timeSheet = new TimeSheetParserResponse();
        timeSheet.setFrom(LocalTime.parse(parts[0].trim()));
        timeSheet.setTo(LocalTime.parse(parts[1].trim()));
        return timeSheet;
    }
}
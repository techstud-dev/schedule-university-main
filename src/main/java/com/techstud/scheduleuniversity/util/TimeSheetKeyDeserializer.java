package com.techstud.scheduleuniversity.util;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.techstud.scheduleuniversity.dao.document.TimeSheet;

import java.io.IOException;
import java.time.LocalTime;

public class TimeSheetKeyDeserializer extends KeyDeserializer {


    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        // Split the string "from-to" into parts
        String[] parts = key.replace("TimeSheet(from=", "").replace(")", "").split(", to=");
        return TimeSheet.builder()
                .from(LocalTime.parse(parts[0].trim()))
                .to(LocalTime.parse(parts[1].trim()))
                .build();
    }
}
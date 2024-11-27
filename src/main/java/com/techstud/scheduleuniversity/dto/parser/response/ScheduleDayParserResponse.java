package com.techstud.scheduleuniversity.dto.parser.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.techstud.scheduleuniversity.util.TimeSheetKeyDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ScheduleDayParserResponse implements Serializable {

    private Date date;

    @JsonDeserialize(keyUsing = TimeSheetKeyDeserializer.class)
    private Map<TimeSheetParserResponse, List<ScheduleObjectParserResponse>> lessons = new LinkedHashMap<>();

}

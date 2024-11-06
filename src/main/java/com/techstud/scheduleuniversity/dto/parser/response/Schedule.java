package com.techstud.scheduleuniversity.dto.parser.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Schedule implements Serializable {

    private Map<DayOfWeek, ScheduleDay> evenWeekSchedule;

    private Map<DayOfWeek, ScheduleDay> oddWeekSchedule;

    private Date snapshotDate = new Date();


    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

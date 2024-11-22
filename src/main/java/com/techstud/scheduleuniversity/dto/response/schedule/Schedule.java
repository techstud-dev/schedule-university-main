package com.techstud.scheduleuniversity.dto.response.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"evenWeekSchedule", "oddWeekSchedule"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Schedule implements Serializable {

    private Map<String, ScheduleDay> evenWeekSchedule;

    private Map<String, ScheduleDay> oddWeekSchedule;

    private String snapshotDate;

}

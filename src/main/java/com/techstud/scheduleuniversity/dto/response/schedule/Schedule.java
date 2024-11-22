package com.techstud.scheduleuniversity.dto.response.schedule;

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
public class Schedule implements Serializable {

    private Map<String, ScheduleDay> evenWeekSchedule;

    private Map<String, ScheduleDay> oddWeekSchedule;

    private String snapshotDate;

}

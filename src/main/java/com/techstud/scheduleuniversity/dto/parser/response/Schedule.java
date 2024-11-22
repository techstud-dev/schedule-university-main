package com.techstud.scheduleuniversity.dto.parser.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule implements Serializable {

    private Map<DayOfWeek, ScheduleDay> evenWeekSchedule;

    private Map<DayOfWeek, ScheduleDay> oddWeekSchedule;

    private Date snapshotDate = new Date();

}

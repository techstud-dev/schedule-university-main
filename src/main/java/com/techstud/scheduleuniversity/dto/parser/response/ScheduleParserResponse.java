package com.techstud.scheduleuniversity.dto.parser.response;

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
public class ScheduleParserResponse implements Serializable {

    private Map<DayOfWeek, ScheduleDayParserResponse> evenWeekSchedule;

    private Map<DayOfWeek, ScheduleDayParserResponse> oddWeekSchedule;

    private Date snapshotDate = new Date();

}

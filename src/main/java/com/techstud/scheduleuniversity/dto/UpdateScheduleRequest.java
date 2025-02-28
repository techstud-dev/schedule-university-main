package com.techstud.scheduleuniversity.dto;

import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UpdateScheduleRequest {

    private List<ScheduleItem> evenWeek;
    private List<ScheduleItem> oddWeek;
    private LocalDate snapshotDate;
}

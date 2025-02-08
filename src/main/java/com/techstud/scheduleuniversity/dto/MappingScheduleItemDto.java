package com.techstud.scheduleuniversity.dto;

import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MappingScheduleItemDto {

    private String universityShortName;
    private ScheduleItem scheduleItem;
}

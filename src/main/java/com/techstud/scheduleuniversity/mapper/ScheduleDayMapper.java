package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.entity.schedule.ScheduleDay;
import com.techstud.scheduleuniversity.entity.schedule.TimeSheet;
import com.techstud.scheduleuniversity.entity.schedule.University;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@RequiredArgsConstructor
@Component
public class ScheduleDayMapper {

    private final TimeSheetMapper timeSheetMapper;
    private final ScheduleObjectMapper scheduleObjectMapper;

    public ScheduleDay mapDtoToEntity(com.techstud.scheduleuniversity.dto.parser.response.ScheduleDay dto, University university) {
        ScheduleDay scheduleDay = new ScheduleDay();
        scheduleDay.setLessons(new LinkedHashMap<>());
        dto.getLessons().forEach((key, value) -> {
            TimeSheet timeSheet = timeSheetMapper.mapDtoToEntity(key, university);
            scheduleDay.getLessons().put(timeSheet, scheduleObjectMapper.mapDtoToEntity(value, timeSheet, university));
        });
        return scheduleDay;
    }
}

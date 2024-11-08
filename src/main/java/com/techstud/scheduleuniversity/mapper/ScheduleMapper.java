package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.entity.schedule.Schedule;
import com.techstud.scheduleuniversity.entity.schedule.ScheduleDay;
import com.techstud.scheduleuniversity.entity.schedule.University;
import com.techstud.scheduleuniversity.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class ScheduleMapper {

    private final ScheduleDayMapper scheduleDayMapper;

    public Schedule mapDtoToEntity(com.techstud.scheduleuniversity.dto.parser.response.Schedule dto, University university) {
        Schedule schedule = null;
        if(university != null) {
            schedule = new Schedule();
            schedule.setSnapshotDate(dto.getSnapshotDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            schedule.setEvenWeekSchedule(parseWeekSchedule(dto.getEvenWeekSchedule(), university));
            schedule.setOddWeekSchedule(parseWeekSchedule(dto.getOddWeekSchedule(), university));
        }
        return schedule;
    }

    private Map<DayOfWeek, ScheduleDay> parseWeekSchedule(Map<DayOfWeek, com.techstud.scheduleuniversity.dto.parser.response.ScheduleDay> dto, University university) {
        Map<DayOfWeek, ScheduleDay> result = new LinkedHashMap<>();
        dto.forEach((key, value) -> {
            ScheduleDay scheduleDay = scheduleDayMapper.mapDtoToEntity(value, university);
            scheduleDay.setDate(value.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            result.put(key, scheduleDay);
        });
        return result;
    }
}

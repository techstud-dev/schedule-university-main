package com.techstud.scheduleuniversity.repository.mongo;

import com.techstud.scheduleuniversity.dao.document.Schedule;
import com.techstud.scheduleuniversity.dao.document.ScheduleDay;
import com.techstud.scheduleuniversity.dao.document.ScheduleObject;
import com.techstud.scheduleuniversity.dao.document.TimeSheet;
import com.techstud.scheduleuniversity.mapper.ScheduleObjectMapper;
import com.techstud.scheduleuniversity.mapper.TimeSheetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleRepositoryFacade {

    private final ScheduleDayRepository scheduleDayRepository;
    private final ScheduleObjectRepository scheduleObjectRepository;
    private final ScheduleRepository scheduleRepository;
    private final TimeSheetRepository timeSheetRepository;
    private final ScheduleObjectMapper scheduleObjectMapper;
    private final TimeSheetMapper timeSheetMapper;


    public Schedule cascadeSave(com.techstud.scheduleuniversity.dto.parser.response.Schedule schedule) {
        Schedule resultAfterSave = new Schedule();

        resultAfterSave.setSnapshotDate(schedule.getSnapshotDate());
        resultAfterSave.setEvenWeekSchedule(cascadeWeekSave(schedule.getEvenWeekSchedule()));
        resultAfterSave.setOddWeekSchedule(cascadeWeekSave(schedule.getOddWeekSchedule()));

        return scheduleRepository.save(resultAfterSave);
    }

    public Map<DayOfWeek, ScheduleDay> cascadeWeekSave(Map<DayOfWeek, com.techstud.scheduleuniversity.dto.parser.response.ScheduleDay> weekSchedule) {
        Map<DayOfWeek, ScheduleDay> result = new LinkedHashMap<>();

        weekSchedule.forEach((dayOfWeek, scheduleDay) -> {
            result.put(dayOfWeek, cascadeDaySave(scheduleDay));
        });

        return result;
    }

    public ScheduleDay cascadeDaySave(com.techstud.scheduleuniversity.dto.parser.response.ScheduleDay scheduleDay) {
        ScheduleDay result = new ScheduleDay();

        result.setDate(scheduleDay.getDate());
        result.setLessons(cascadeLessonSave(scheduleDay.getLessons()));

        return scheduleDayRepository.save(result);
    }

    public Map<String, List<ScheduleObject>> cascadeLessonSave(
            Map<com.techstud.scheduleuniversity.dto.parser.response.TimeSheet,
                    List<com.techstud.scheduleuniversity.dto.parser.response.ScheduleObject>> lessons) {
        Map<String, List<ScheduleObject>> result = new LinkedHashMap<>();

        lessons.forEach((timeSheet, scheduleObjects) -> {
            TimeSheet documentTimeSheet = timeSheetMapper.toDocument(timeSheet);

            result.put(timeSheetRepository.save(documentTimeSheet).getId(),
                    scheduleObjectRepository.saveAll(scheduleObjectMapper.toDocument(scheduleObjects)));
        });

        return result;
    }


}

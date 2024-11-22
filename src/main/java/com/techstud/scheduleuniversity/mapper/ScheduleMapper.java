package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dao.document.ScheduleDay;
import com.techstud.scheduleuniversity.dao.document.TimeSheet;
import com.techstud.scheduleuniversity.dto.response.schedule.Schedule;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleObject;
import com.techstud.scheduleuniversity.repository.mongo.TimeSheetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ScheduleMapper {

    private final TimeSheetRepository timeSheetRepository;

    private static final Map<DayOfWeek, String> RU_DAYS_OF_WEEK = Map.of(
            DayOfWeek.MONDAY, "Понедельник",
            DayOfWeek.TUESDAY, "Вторник",
            DayOfWeek.WEDNESDAY, "Среда",
            DayOfWeek.THURSDAY, "Четверг",
            DayOfWeek.FRIDAY, "Пятница",
            DayOfWeek.SATURDAY, "Суббота",
            DayOfWeek.SUNDAY, "Воскресенье"
    );

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public Schedule toResponse(com.techstud.scheduleuniversity.dao.document.Schedule documentSchedule) {
        return new Schedule(
                mapDocumentWeek(documentSchedule.getEvenWeekSchedule()),
                mapDocumentWeek(documentSchedule.getOddWeekSchedule()),
                formatDate(documentSchedule.getSnapshotDate())
        );
    }

    private Map<String, com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDay> mapDocumentWeek(Map<DayOfWeek, ScheduleDay> documentWeek) {
        Map<String, com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDay> response = new LinkedHashMap<>();
        documentWeek.forEach((dayOfWeek, scheduleDay) ->
                response.put(getRuDayOfWeek(dayOfWeek), mapDocumentDay(scheduleDay))
        );
        return response;
    }

    private com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDay mapDocumentDay(ScheduleDay documentDay) {
        return new com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDay(
                formatDate(documentDay.getDate()),
                mapDocumentLessons(documentDay.getLessons())
        );
    }

    private Map<String, List<ScheduleObject>> mapDocumentLessons(Map<String, List<com.techstud.scheduleuniversity.dao.document.ScheduleObject>> documentLessons) {
        Map<String, List<ScheduleObject>> response = new LinkedHashMap<>();
        documentLessons.forEach((key, value) ->
                timeSheetRepository.findById(key).ifPresent(timeSheet ->
                        response.put(getTimeInterval(timeSheet), mapDocumentObjects(value))
                )
        );
        return response;
    }

    private List<ScheduleObject> mapDocumentObjects(List<com.techstud.scheduleuniversity.dao.document.ScheduleObject> documentObjects) {
        List<ScheduleObject> response = new ArrayList<>();
        for (var documentObject : documentObjects) {
            response.add(new ScheduleObject(
                    documentObject.getType().getRuName(),
                    documentObject.getName(),
                    documentObject.getTeacher(),
                    documentObject.getPlace(),
                    documentObject.getGroups()
            ));
        }
        return response;
    }

    private String getRuDayOfWeek(DayOfWeek dayOfWeek) {
        return RU_DAYS_OF_WEEK.getOrDefault(dayOfWeek, "Неизвестный день");
    }

    private String formatDate(Date date) {
        return DATE_FORMATTER.format(date);
    }

    private String getTimeInterval(TimeSheet timeSheet) {
        return timeSheet.getFrom().format(TIME_FORMATTER) + " - " + timeSheet.getTo().format(TIME_FORMATTER);
    }
}

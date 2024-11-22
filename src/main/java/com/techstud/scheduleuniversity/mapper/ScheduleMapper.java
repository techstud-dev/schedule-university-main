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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ScheduleMapper {

    private final TimeSheetRepository timeSheetRepository;

    public Schedule toResponse(com.techstud.scheduleuniversity.dao.document.Schedule documentSchedule) {
        Schedule scheduleResult = new Schedule();
        scheduleResult.setEvenWeekSchedule(mapDocumentWeek(documentSchedule.getEvenWeekSchedule()));
        scheduleResult.setOddWeekSchedule(mapDocumentWeek(documentSchedule.getOddWeekSchedule()));
        scheduleResult.setSnapshotDate(getRuFormatDate(documentSchedule.getSnapshotDate()));
        return scheduleResult;
    }

    private Map<String, com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDay> mapDocumentWeek(Map<DayOfWeek, ScheduleDay> documentWeek) {
        Map<String, com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDay> response = new LinkedHashMap<>();

        documentWeek.forEach((dayOfWeek, scheduleDay) -> {
            response.put(getRuDayOfWeek(dayOfWeek), mapDocumentDay(scheduleDay));
        });

        return response;
    }

    private com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDay mapDocumentDay(ScheduleDay documentDay) {
        com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDay response = new com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDay();
        response.setDate(getRuFormatDate(documentDay.getDate()));
        response.setLessons(mapDocumentLessons(documentDay.getLessons()));
        return response;
    }

    private Map<String, java.util.List<com.techstud.scheduleuniversity.dto.response.schedule.ScheduleObject>> mapDocumentLessons(Map<String, java.util.List<com.techstud.scheduleuniversity.dao.document.ScheduleObject>> documentLessons) {
        Map<String, java.util.List<com.techstud.scheduleuniversity.dto.response.schedule.ScheduleObject>> response = new LinkedHashMap<>();
        documentLessons.forEach((key, value) -> {
            timeSheetRepository.findById(key).ifPresent(timeSheet -> response.put(getTimeInterval(timeSheet), mapDocumentObjects(value)));
        });

        return response;
    }

    private List<ScheduleObject> mapDocumentObjects(List<com.techstud.scheduleuniversity.dao.document.ScheduleObject> documentObjects) {
        List<ScheduleObject> response = new java.util.ArrayList<>();
        documentObjects.forEach(documentObject -> {
            ScheduleObject scheduleObject = new ScheduleObject();
            scheduleObject.setGroups(documentObject.getGroups());
            scheduleObject.setPlace(documentObject.getPlace());
            scheduleObject.setTeacher(documentObject.getTeacher());
            scheduleObject.setName(documentObject.getName());
            scheduleObject.setType(documentObject.getType().getRuName());
            response.add(scheduleObject);
        });
        return response;
    }

    private String getRuDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Понедельник";
            case TUESDAY -> "Вторник";
            case WEDNESDAY -> "Среда";
            case THURSDAY -> "Четверг";
            case FRIDAY -> "Пятница";
            case SATURDAY -> "Суббота";
            case SUNDAY -> "Воскресенье";
        };
    }

    private String getRuFormatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        return formatter.format(date);
    }

    private String getTimeInterval(TimeSheet timeSheet) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return timeSheet.getFrom().format(formatter) + " - " + timeSheet.getTo().format(formatter);
    }
}

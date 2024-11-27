package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDayDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleObjectDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.TimeSheetDocument;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDayApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleObjectApiResponse;
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

    public ScheduleApiResponse toResponse(ScheduleDocument documentSchedule) {
        return new ScheduleApiResponse(
                mapDocumentWeek(documentSchedule.getEvenWeekSchedule()),
                mapDocumentWeek(documentSchedule.getOddWeekSchedule()),
                formatDate(documentSchedule.getSnapshotDate())
        );
    }

    private Map<String, ScheduleDayApiResponse> mapDocumentWeek(Map<DayOfWeek, ScheduleDayDocument> documentWeek) {
        Map<String, ScheduleDayApiResponse> response = new LinkedHashMap<>();
        documentWeek.forEach((dayOfWeek, scheduleDay) ->
                response.put(getRuDayOfWeek(dayOfWeek), mapDocumentDay(scheduleDay))
        );
        return response;
    }

    private ScheduleDayApiResponse mapDocumentDay(ScheduleDayDocument documentDay) {
        return new ScheduleDayApiResponse(
                formatDate(documentDay.getDate()),
                mapDocumentLessons(documentDay.getLessons())
        );
    }

    private Map<String, List<ScheduleObjectApiResponse>> mapDocumentLessons(Map<String, List<ScheduleObjectDocument>> documentLessons) {
        Map<String, List<ScheduleObjectApiResponse>> response = new LinkedHashMap<>();
        documentLessons.forEach((key, value) ->
                timeSheetRepository.findById(key).ifPresent(timeSheet ->
                        response.put(getTimeInterval(timeSheet), mapDocumentObjects(value))
                )
        );
        return response;
    }

    private List<ScheduleObjectApiResponse> mapDocumentObjects(List<ScheduleObjectDocument> documentObjects) {
        List<ScheduleObjectApiResponse> response = new ArrayList<>();
        for (var documentObject : documentObjects) {
            response.add(new ScheduleObjectApiResponse(
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
        if (date != null) {
            return DATE_FORMATTER.format(date);
        } else
            return null;
    }

    private String getTimeInterval(TimeSheetDocument timeSheet) {
        return timeSheet.getFrom().format(TIME_FORMATTER) + " - " + timeSheet.getTo().format(TIME_FORMATTER);
    }
}

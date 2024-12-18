package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.controller.ScheduleController;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDayDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;
import com.techstud.scheduleuniversity.exception.StudentNotFoundException;
import com.techstud.scheduleuniversity.repository.mongo.TimeSheetRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@RequiredArgsConstructor
@Slf4j
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

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @SneakyThrows
    public CollectionModel<EntityModel<ScheduleItem>> toResponse(ScheduleDocument documentSchedule,
                                                                 String scheduleDayId,
                                                                 String timeWindowId) {
        var oddWeekSchedulesDocument = documentSchedule.getOddWeekSchedule();
        var evenWeekSchedulesDocument = documentSchedule.getEvenWeekSchedule();

        oddWeekSchedulesDocument.entrySet().removeIf(entry -> !entry.getValue().getId().equals(scheduleDayId));
        evenWeekSchedulesDocument.entrySet().removeIf(entry -> !entry.getValue().getId().equals(scheduleDayId));

        oddWeekSchedulesDocument.forEach((dayOfWeek, scheduleDay) -> {
            var lessons = new HashMap<>(scheduleDay.getLessons());
            lessons.forEach((timeWindowId1, scheduleObjects) -> {
                if (!timeWindowId1.equals(timeWindowId)) {
                    scheduleDay.getLessons().remove(timeWindowId1); // Безопасное удаление
                }
            });
        });

        evenWeekSchedulesDocument.forEach((dayOfWeek, scheduleDay) -> {
            var lessons = new HashMap<>(scheduleDay.getLessons());
            lessons.forEach((timeWindowId1, scheduleObjects) -> {
                if (!timeWindowId1.equals(timeWindowId)) {
                    scheduleDay.getLessons().remove(timeWindowId1); // Безопасное удаление
                }
            });
        });

        List<EntityModel<ScheduleItem>> result;
        if (!oddWeekSchedulesDocument.isEmpty()) {
            result = mapWeek(oddWeekSchedulesDocument, false);
        } else {
            result = mapWeek(evenWeekSchedulesDocument, true);
        }
        return CollectionModel.of(result);
    }



    @SneakyThrows
    public EntityModel<ScheduleApiResponse> toResponse(ScheduleDocument documentSchedule) {
        ScheduleApiResponse response = new ScheduleApiResponse();

        List<EntityModel<ScheduleItem>> oddWeekSchedules = mapWeek(documentSchedule.getOddWeekSchedule(), false);
        List<EntityModel<ScheduleItem>> evenWeekSchedules = mapWeek(documentSchedule.getEvenWeekSchedule(), true);

        LocalDate snapshotDate = LocalDate.ofInstant(documentSchedule.getSnapshotDate().toInstant(), TimeZone.getDefault().toZoneId());
        response.setEvenWeekSchedules(evenWeekSchedules);
        response.setOddWeekSchedules(oddWeekSchedules);
        response.setSnapshotDate(snapshotDate);

        return EntityModel.of(response,
                linkTo(
                        methodOn(ScheduleController.class).getSchedule(documentSchedule.getId(), null))
                        .withRel("getSchedule")
                        .withType("GET"),
                linkTo(
                        methodOn(ScheduleController.class).updateSchedule(documentSchedule.getId(),
                                null, null))
                        .withRel("updateSchedule")
                        .withType("PUT"),
                linkTo(
                        methodOn(ScheduleController.class).deleteSchedule(documentSchedule.getId(), null))
                        .withRel("deleteSchedule")
                        .withType("DELETE"),
                linkTo(
                        methodOn(ScheduleController.class).importSchedule(null,null))
                        .withRel("importSchedule")
                        .withType("POST"),
                linkTo(
                        methodOn(ScheduleController.class).forceImportSchedule(null,null))
                        .withRel("forceImportSchedule")
                        .withType("POST"),
                linkTo(
                        methodOn(ScheduleController.class).createSchedule(null, null))
                        .withRel("createSchedule")
                        .withType("POST")
                );
    }


    private List<EntityModel<ScheduleItem>> mapWeek(Map<DayOfWeek, ScheduleDayDocument> documentWeek, boolean isEven) {
        List<EntityModel<ScheduleItem>> response = new ArrayList<>();
        documentWeek.forEach((dayOfWeek, scheduleDay) -> {
            scheduleDay.getLessons().forEach((timeWindowId, scheduleObjects) -> {
                scheduleObjects.forEach(scheduleObject -> {
                    ScheduleItem scheduleItem = new ScheduleItem();
                    scheduleItem.setId(scheduleObject.getId());
                    scheduleItem.setEven(isEven);
                    scheduleItem.setDayOfWeek(RU_DAYS_OF_WEEK.get(dayOfWeek));
                    scheduleItem.setDate(mapDate(scheduleDay.getDate(), dayOfWeek));
                    scheduleItem.setTime(mapTime(timeWindowId));
                    scheduleItem.setType(scheduleObject.getType().getRuName());
                    scheduleItem.setName(scheduleObject.getName());
                    scheduleItem.setTeacher(scheduleObject.getTeacher() == null ? "-" : scheduleObject.getTeacher());
                    scheduleItem.setPlace(scheduleObject.getPlace());
                    scheduleItem.setGroups(scheduleObject.getGroups());

                    EntityModel<ScheduleItem> scheduleItemEntity = null;
                    try {
                        scheduleItemEntity = EntityModel.of(scheduleItem,
                                linkTo(
                                        methodOn(ScheduleController.class).getLesson(scheduleDay.getId().toString(), timeWindowId,
                                                null))
                                        .withRel("getScheduleObject")
                                        .withType("GET"),
                                linkTo(
                                        methodOn(ScheduleController.class).updateLesson(scheduleDay.getId(), timeWindowId,
                                                null, null))
                                        .withRel("updateScheduleObject")
                                        .withType("PUT"),
                                linkTo(
                                        methodOn(ScheduleController.class).deleteLesson(scheduleDay.getId(), timeWindowId,
                                                null))
                                        .withRel("deleteScheduleObject")
                                        .withType("DELETE"),
                                linkTo(
                                        methodOn(ScheduleController.class).saveLesson(null, null))
                                        .withRel("createScheduleObject")
                                        .withType("POST"),
                                linkTo(
                                        methodOn(ScheduleController.class).getScheduleDay(scheduleDay.getId(), null))
                                        .withRel("getScheduleDay")
                                        .withType("GET"),
                                linkTo(
                                        methodOn(ScheduleController.class).createScheduleDay(null, null))
                                        .withRel("createScheduleDay")
                                        .withType("POST"),
                                linkTo(
                                        methodOn(ScheduleController.class).updateScheduleDay(scheduleDay.getId(),
                                                null, null))
                                        .withRel("updateScheduleDay")
                                        .withType("PUT"),
                                linkTo(
                                        methodOn(ScheduleController.class).deleteScheduleDay(scheduleDay.getId(), null))
                                        .withRel("deleteScheduleDay")
                                        .withType("DELETE")
                        );
                    } catch (ScheduleNotFoundException | StudentNotFoundException ignored) {}
                    response.add(scheduleItemEntity);
                });
            });
        });
        return response;
    }



    private long mapDate(Date date, DayOfWeek dayOfWeek) {
        if (date != null) {
            return date.getTime();
        } else {
            Date currentDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int daysDiff = dayOfWeek.getValue() - currentDayOfWeek;
            Date resultDate = new Date(currentDate.getTime() +
                    (long) daysDiff * 24 * 60 * 60 * 1000);
            return resultDate.getTime();
        }
    }

    private String mapTime(String timeSheetId) {
        return timeSheetRepository.findById(timeSheetId)
                .map(timeSheet -> timeSheet.getFrom().format(TIME_FORMATTER) + " - " +
                        timeSheet.getTo().format(TIME_FORMATTER))
                .orElse(null);
    }
}

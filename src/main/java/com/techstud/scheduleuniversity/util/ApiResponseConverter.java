package com.techstud.scheduleuniversity.util;

import com.techstud.scheduleuniversity.controller.ScheduleController;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDayDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleObjectDocument;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDayApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleObjectApiResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.*;

@Component
public class ApiResponseConverter {

    private static final Map<String, DayOfWeek> RUSSIAN_DAY_OF_WEEK_MAP = new HashMap<>();

    static {
        RUSSIAN_DAY_OF_WEEK_MAP.put("ПОНЕДЕЛЬНИК", DayOfWeek.MONDAY);
        RUSSIAN_DAY_OF_WEEK_MAP.put("ВТОРНИК", DayOfWeek.TUESDAY);
        RUSSIAN_DAY_OF_WEEK_MAP.put("СРЕДА", DayOfWeek.WEDNESDAY);
        RUSSIAN_DAY_OF_WEEK_MAP.put("ЧЕТВЕРГ", DayOfWeek.THURSDAY);
        RUSSIAN_DAY_OF_WEEK_MAP.put("ПЯТНИЦА", DayOfWeek.FRIDAY);
        RUSSIAN_DAY_OF_WEEK_MAP.put("СУББОТА", DayOfWeek.SATURDAY);
        RUSSIAN_DAY_OF_WEEK_MAP.put("ВОСКРЕСЕНЬЕ", DayOfWeek.SUNDAY);
    }

    public EntityModel<ScheduleApiResponse> convertToEntityModel(ScheduleApiResponse response, ScheduleDocument scheduleDocument) {
        EntityModel<ScheduleApiResponse> entityModel = EntityModel.of(response);


        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ScheduleController.class)
                        .getSchedule(scheduleDocument.getId(), null))
                .withSelfRel();
        Link updateLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ScheduleController.class)
                        .updateSchedule(scheduleDocument.getId(), null, null))
                .withRel("update");
        Link deleteLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ScheduleController.class)
                        .deleteSchedule(scheduleDocument.getId(), null))
                .withRel("delete");
        entityModel.add(selfLink, updateLink, deleteLink);


        processScheduleDays(response.getEvenWeekSchedule(), scheduleDocument.getEvenWeekSchedule());
        processScheduleDays(response.getOddWeekSchedule(), scheduleDocument.getOddWeekSchedule());

        return entityModel;
    }

    private void processScheduleDays(Map<String, ScheduleDayApiResponse> dayResponses,
                                     Map<DayOfWeek, ScheduleDayDocument> dayDocuments) {
        if (dayResponses != null && dayDocuments != null) {
            for (Map.Entry<String, ScheduleDayApiResponse> entry : dayResponses.entrySet()) {
                String dayKey = entry.getKey();
                ScheduleDayApiResponse dayResponse = entry.getValue();

                DayOfWeek dayOfWeek = RUSSIAN_DAY_OF_WEEK_MAP.get(dayKey.toUpperCase());
                if (dayOfWeek == null) {
                    continue;
                }

                ScheduleDayDocument dayDocument = dayDocuments.get(dayOfWeek);

                if (dayDocument != null) {
                    addLinksToScheduleDay(dayResponse, dayDocument);
                }
            }
        }
    }

    private void addLinksToScheduleDay(ScheduleDayApiResponse dayResponse, ScheduleDayDocument dayDocument) {

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ScheduleController.class)
                        .getScheduleDay(dayDocument.getId(), null))
                .withSelfRel();
        Link updateLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ScheduleController.class)
                        .updateScheduleDay(dayDocument.getId(), null, null))
                .withRel("update");
        Link deleteLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ScheduleController.class)
                        .deleteScheduleDay(dayDocument.getId(), null))
                .withRel("delete");
        dayResponse.add(selfLink, updateLink, deleteLink);

        Map<String, List<ScheduleObjectApiResponse>> lessonsResponse = dayResponse.getLessons();
        Map<String, List<ScheduleObjectDocument>> lessonsDocument = dayDocument.getLessons();

        if (lessonsResponse != null && lessonsDocument != null) {
            Iterator<Map.Entry<String, List<ScheduleObjectApiResponse>>> responseIterator = lessonsResponse.entrySet().iterator();
            Iterator<Map.Entry<String, List<ScheduleObjectDocument>>> documentIterator = lessonsDocument.entrySet().iterator();

            while (responseIterator.hasNext() && documentIterator.hasNext()) {
                Map.Entry<String, List<ScheduleObjectApiResponse>> responseEntry = responseIterator.next();
                Map.Entry<String, List<ScheduleObjectDocument>> documentEntry = documentIterator.next();

                String timeWindowResponse = responseEntry.getKey();
                String timeWindowDocument = documentEntry.getKey();

                List<ScheduleObjectApiResponse> lessonResponses = responseEntry.getValue();
                List<ScheduleObjectDocument> lessonDocuments = documentEntry.getValue();


                for (int i = 0; i < lessonResponses.size(); i++) {
                    ScheduleObjectApiResponse lessonResponse = lessonResponses.get(i);
                    ScheduleObjectDocument lessonDocument = lessonDocuments.get(i);

                    addLinksToLesson(lessonResponse, dayDocument.getId(), timeWindowResponse, lessonDocument);
                }
            }
        }
    }

    private void addLinksToLesson(ScheduleObjectApiResponse lessonResponse,
                                  String scheduleDayId, String timeWindow,
                                  ScheduleObjectDocument lessonDocument) {

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ScheduleController.class)
                        .getLesson(scheduleDayId, timeWindow, null))
                .withSelfRel();
        Link updateLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ScheduleController.class)
                        .updateLesson(scheduleDayId, timeWindow, null, null))
                .withRel("update");
        Link deleteLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ScheduleController.class)
                        .deleteLesson(scheduleDayId, timeWindow, null))
                .withRel("delete");
        lessonResponse.add(selfLink, updateLink, deleteLink);
    }
}

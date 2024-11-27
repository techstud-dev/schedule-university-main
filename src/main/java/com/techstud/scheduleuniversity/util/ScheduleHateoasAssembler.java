package com.techstud.scheduleuniversity.util;

import com.techstud.scheduleuniversity.controller.ScheduleController;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDayApiResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ScheduleHateoasAssembler {

    public static EntityModel<ScheduleApiResponse> toModel(ScheduleApiResponse schedule, ScheduleDocument scheduleDocument) {

        EntityModel<ScheduleApiResponse> scheduleModel = EntityModel.of(schedule);

        scheduleModel.add(linkTo(methodOn(ScheduleController.class).getSchedule(scheduleDocument.getId())).withSelfRel());
        scheduleModel.add(linkTo(methodOn(ScheduleController.class).updateSchedule(scheduleDocument.getId(), null)).withRel("update"));
        scheduleModel.add(linkTo(methodOn(ScheduleController.class).deleteSchedule(scheduleDocument.getId())).withRel("delete"));

        //FIXME: Продумать все ссылки, которые будут уходить на UI

        if (schedule.getEvenWeekSchedule() != null) {
            schedule.setEvenWeekSchedule(createScheduleDayModels(schedule.getEvenWeekSchedule(), scheduleDocument.getId(), "evenWeek"));
        }
        if (schedule.getOddWeekSchedule() != null) {
            schedule.setOddWeekSchedule(createScheduleDayModels(schedule.getOddWeekSchedule(), scheduleDocument.getId(), "oddWeek"));
        }

        return scheduleModel;
    }

    private static Map<String, ScheduleDayApiResponse> createScheduleDayModels(Map<String, ScheduleDayApiResponse> weekSchedule, String scheduleId, String weekType) {
        Map<String, ScheduleDayApiResponse> hateoasScheduleDays = new LinkedHashMap<>();

        weekSchedule.forEach((day, scheduleDay) -> {
            Link selfLink = linkTo(methodOn(ScheduleController.class)
                    .getScheduleDay(scheduleId, weekType, day))
                    .withSelfRel();

            if (scheduleDay.getLessons() != null) {
                scheduleDay.getLessons().forEach((time, lessons) -> {
                    lessons.forEach(lesson -> {

                        lesson.add(linkTo(methodOn(ScheduleController.class)
                                .getLesson(scheduleId, weekType, day, time))
                                .withSelfRel());
                    });
                });
            }

            EntityModel<ScheduleDayApiResponse> scheduleDayModel = EntityModel.of(scheduleDay, selfLink);

            hateoasScheduleDays.put(day, scheduleDayModel.getContent());
        });

        return hateoasScheduleDays;
    }
}

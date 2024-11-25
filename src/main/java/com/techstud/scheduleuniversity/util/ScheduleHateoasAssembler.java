package com.techstud.scheduleuniversity.util;

import com.techstud.scheduleuniversity.controller.ScheduleController;
import com.techstud.scheduleuniversity.dto.response.schedule.Schedule;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ScheduleHateoasAssembler {

    public static EntityModel<Schedule> toModel(Schedule schedule, com.techstud.scheduleuniversity.dao.document.Schedule scheduleDocument) {

        EntityModel<Schedule> scheduleModel = EntityModel.of(schedule);

        scheduleModel.add(linkTo(methodOn(ScheduleController.class).getSchedule(scheduleDocument.getId())).withSelfRel());
        scheduleModel.add(linkTo(methodOn(ScheduleController.class).updateSchedule(scheduleDocument.getId(), null)).withRel("update"));
        scheduleModel.add(linkTo(methodOn(ScheduleController.class).deleteSchedule(scheduleDocument.getId())).withRel("delete"));

        if (schedule.getEvenWeekSchedule() != null) {
            schedule.setEvenWeekSchedule(createScheduleDayModels(schedule.getEvenWeekSchedule(), scheduleDocument.getId(), "evenWeek"));
        }
        if (schedule.getOddWeekSchedule() != null) {
            schedule.setOddWeekSchedule(createScheduleDayModels(schedule.getOddWeekSchedule(), scheduleDocument.getId(), "oddWeek"));
        }

        return scheduleModel;
    }

    private static Map<String, com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDay> createScheduleDayModels(Map<String, com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDay> weekSchedule, String scheduleId, String weekType) {
        Map<String,  com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDay> hateoasScheduleDays = new LinkedHashMap<>();

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

            EntityModel<com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDay> scheduleDayModel = EntityModel.of(scheduleDay, selfLink);

            hateoasScheduleDays.put(day, scheduleDayModel.getContent());
        });

        return hateoasScheduleDays;
    }
}

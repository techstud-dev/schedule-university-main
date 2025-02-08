package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.controller.ScheduleController;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.entity.Group;
import com.techstud.scheduleuniversity.entity.Lesson;
import com.techstud.scheduleuniversity.entity.TimeSheet;
import lombok.SneakyThrows;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class LessonMapper implements MultipleMapper<Lesson, EntityModel<ScheduleItem>> {

    @Override
    @SneakyThrows
    public EntityModel<ScheduleItem> map(Lesson lesson) {
        String id = lesson.getId() != null ? lesson.getId().toString() : "-";
        boolean isEven = lesson.isEvenWeek();
        String dayOfWeek = lesson.getDayOfWeek() != null ? lesson.getDayOfWeek().toString() : "-";

        long date = 0L; //TODO: Future problem

        String time = formatTime(lesson.getTimeSheet());

        String type = lesson.getType() != null ? lesson.getType().toString() : "Другое";
        String name = lesson.getName();
        String teacher = lesson.getTeacher() != null ? lesson.getTeacher().getTeacherName() : "-";
        String place = lesson.getPlace() != null ? lesson.getPlace().getPlaceName() : "-";
        List<String> groups = lesson.getGroups() != null
                ? lesson.getGroups().stream().map(Group::getGroupCode).collect(Collectors.toList())
                : Collections.emptyList();

        ScheduleItem resultScheduleItem = ScheduleItem.builder()
                .id(id)
                .isEven(isEven)
                .dayOfWeek(dayOfWeek)
                .date(date)
                .time(time)
                .type(type)
                .name(name)
                .teacher(teacher)
                .place(place)
                .groups(groups)
                .build();

        return EntityModel
                .of(resultScheduleItem)
                .add(linkTo(
                        methodOn(ScheduleController.class)
                                .getLesson(lesson.getDayOfWeek().toString(), lesson.getTimeSheet().getId(), null))
                        .withRel("GET")
                        .withName("getLesson"))
                .add(linkTo(
                        methodOn(ScheduleController.class)
                                .saveLesson(null, null))
                        .withRel("POST")
                        .withName("createLesson"))
                .add(linkTo(
                        methodOn(ScheduleController.class)
                                .updateLesson(lesson.getDayOfWeek().toString(), lesson.getTimeSheet().getId(), null, null))
                        .withRel("PUT")
                        .withName("updateLesson"))
                .add(linkTo(
                        methodOn(ScheduleController.class)
                                .deleteLesson(lesson.getDayOfWeek().toString(), lesson.getTimeSheet().getId(), null))
                        .withRel("DELETE")
                        .withName("deleteLesson"));
    }

    @Override
    @SneakyThrows
    public CollectionModel<EntityModel<ScheduleItem>> mapAll(List<Lesson> source) {
        return CollectionModel.of(
                        source.stream()
                                .map(this::map)
                                .collect(Collectors.toList()))
                .add(Link.of("/scheduleDay/{dayOfWeek}?isEvenWeek={isEvenWeek}")
                        .withRel("GET")
                        .withName("getScheduleDay"))
                .add(linkTo(methodOn(ScheduleController.class).createScheduleDay(null, null))
                        .withRel("POST")
                        .withName("createScheduleDay"))
                .add(Link.of("/scheduleDay/{dayOfWeek}?isEvenWeek={isEvenWeek}")
                        .withRel("PUT")
                        .withName("updateScheduleDay"))
                .add(Link.of("/scheduleDay/{dayOfWeek}?isEvenWeek={isEvenWeek}")
                        .withRel("DELETE")
                        .withName("deleteScheduleDay"));
    }

    private String formatTime(TimeSheet timeSheet) {
        if (timeSheet == null || timeSheet.getFromTime() == null || timeSheet.getToTime() == null) {
            return "-";
        }
        return timeSheet.getFromTime() + " - " + timeSheet.getToTime();
    }
}

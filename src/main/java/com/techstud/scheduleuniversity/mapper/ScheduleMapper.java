package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.controller.ScheduleController;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.entity.Lesson;
import com.techstud.scheduleuniversity.entity.Schedule;
import com.techstud.scheduleuniversity.entity.TimeSheet;
import lombok.SneakyThrows;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ScheduleMapper implements Mapper<Schedule, EntityModel<ScheduleApiResponse>> {

    @SneakyThrows
    @Override
    @SuppressWarnings("all")
    public EntityModel<ScheduleApiResponse> map(Schedule source) {

        if (source == null) {
            throw new NullPointerException("Schedule must be null for mapping to response!");
        }

        List<Lesson> lessons = source.getLessonList();
        if (lessons == null) {
            lessons = Collections.emptyList();
        }

        List<EntityModel<ScheduleItem>> evenWeekLessons = lessons.stream()
                .filter(Lesson::isEvenWeek)
                .map(this::mapLessonToScheduleItem)
                .collect(Collectors.toList());

        List<EntityModel<ScheduleItem>> oddWeekLessons = lessons.stream()
                .filter(lesson -> !lesson.isEvenWeek())
                .map(this::mapLessonToScheduleItem)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ScheduleItem>> evenWeekLessonCollectionModel =
                CollectionModel.of(evenWeekLessons)
                        .add(
                        Link.of("/scheduleDay/{dayOfWeek}?isEvenWeek={isEvenWeek}")
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

        CollectionModel<EntityModel<ScheduleItem>> oddWeekLessonCollectionModel =
                CollectionModel.of(oddWeekLessons).add(
                                Link.of("/scheduleDay/{dayOfWeek}?isEvenWeek={isEvenWeek}")
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

        ScheduleApiResponse scheduleApiResponse = ScheduleApiResponse.builder()
                .evenWeekSchedules(evenWeekLessonCollectionModel)
                .oddWeekSchedules(oddWeekLessonCollectionModel)
                .snapshotDate(LocalDate.now())
                .build();


        return EntityModel.of(scheduleApiResponse)
                .add(linkTo(
                        methodOn(ScheduleController.class).importSchedule(null, null))
                        .withRel("POST")
                        .withName("importSchedule"))
                .add(linkTo(
                        methodOn(ScheduleController.class).forceImportSchedule(null, null))
                        .withRel("POST")
                        .withName("forceImportSchedule"))
                .add(linkTo(
                        methodOn(ScheduleController.class).getSchedule(source.getId(), null))
                        .withRel("GET")
                        .withName("getSchedule"))
                .add(linkTo(
                        methodOn(ScheduleController.class).getSchedulePostAuthorize(null))
                        .withRel("GET")
                        .withName("getSchedulePostAuthorize"))
                .add(linkTo(
                        methodOn(ScheduleController.class).createSchedule(null, null))
                        .withRel("POST")
                        .withName("createSchedule"))
                .add(linkTo(
                        methodOn(ScheduleController.class).deleteSchedule(source.getId(), null))
                        .withRel("DELETE")
                        .withName("deleteSchedule"));
    }


    @SneakyThrows
    @SuppressWarnings("all")
    private EntityModel<ScheduleItem> mapLessonToScheduleItem(Lesson lesson) {
        String id = lesson.getId() != null ? lesson.getId().toString() : "-";
        boolean isEven = lesson.isEvenWeek();
        String dayOfWeek = lesson.getDayOfWeek() != null ? lesson.getDayOfWeek().toString() : "-";

        long date = 0L; //TODO: Future problem

        String time = formatTime(lesson.getTimeSheet());

        String type = lesson.getType() != null ? lesson.getType().toString() : "Другое";
        String name = lesson.getName();
        String teacher = lesson.getTeacher() != null ? lesson.getTeacher().toString() : "-";
        String place = lesson.getPlace() != null ? lesson.getPlace().toString() : "-";
        List<String> groups = lesson.getGroups() != null
                ? lesson.getGroups().stream().map(Object::toString).collect(Collectors.toList())
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

    private String formatTime(TimeSheet timeSheet) {
        if (timeSheet == null || timeSheet.getFromTime() == null || timeSheet.getToTime() == null) {
            return "-";
        }
        return timeSheet.getFromTime() + " - " + timeSheet.getToTime();
    }
}

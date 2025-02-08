package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.ParserResponseTimeoutException;
import com.techstud.scheduleuniversity.exception.ResourceExistsException;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import java.util.List;

public interface LessonServiceFacade {

    CollectionModel<EntityModel<ScheduleItem>> getLessonsByStudentAndScheduleDay(String username, String dayOfWeek, boolean isEvenWeek)
            throws ScheduleNotFoundException;

    EntityModel<ScheduleApiResponse> createScheduleDay(List<ScheduleItem> savedScheduleItems, String username) throws ResourceExistsException, ScheduleNotFoundException;

    EntityModel<ScheduleApiResponse> deleteLesson(String dayOfWeek, Long timeWindowId, String name) throws ScheduleNotFoundException;

    EntityModel<ScheduleApiResponse> updateLesson(String dayOfWeek, Long timeWindowId, ScheduleItem data, String name);

    EntityModel<ScheduleApiResponse> createLesson(ScheduleItem data, String name);

    CollectionModel<EntityModel<ScheduleItem>> getLessonByStudentAndScheduleDayAndTimeWindow(String name, String dayOfWeek, Long timeWindowId);

    EntityModel<ScheduleApiResponse> deleteScheduleDay(String dayOfWeek, String name, boolean isEvenWeek);

    EntityModel<ScheduleApiResponse> updateScheduleDay(String dayOfWeek, List<ScheduleItem> data, String name, boolean isEvenWeek);
}

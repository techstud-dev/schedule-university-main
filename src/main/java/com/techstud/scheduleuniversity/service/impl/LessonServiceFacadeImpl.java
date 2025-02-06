package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.service.LessonServiceFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LessonServiceFacadeImpl implements LessonServiceFacade {
    @Override
    public CollectionModel<EntityModel<ScheduleItem>> getLessonsByStudentAndScheduleDay(String username, String dayOfWeek, boolean isEvenWeek) {
        return null;
    }

    @Override
    public EntityModel<ScheduleApiResponse> createScheduleDay(List<ScheduleItem> savedScheduleItems, String username) {
        return null;
    }

    @Override
    public EntityModel<ScheduleApiResponse> deleteLesson(String dayOfWeek, Long timeWindowId, String name) {
        return null;
    }

    @Override
    public EntityModel<ScheduleApiResponse> updateLesson(String dayOfWeek, Long timeWindowId, ScheduleItem data, String name) {
        return null;
    }

    @Override
    public EntityModel<ScheduleApiResponse> createLesson(ScheduleItem data, String name) {
        return null;
    }

    @Override
    public CollectionModel<EntityModel<ScheduleItem>> getLessonByStudentAndScheduleDayAndTimeWindow(String name, String dayOfWeek, Long timeWindowId) {
        return null;
    }

    @Override
    public EntityModel<ScheduleApiResponse> deleteScheduleDay(String dayOfWeek, String name, boolean isEvenWeek) {
        return null;
    }

    @Override
    public EntityModel<ScheduleApiResponse> updateScheduleDay(String dayOfWeek, List<ScheduleItem> data, String name, boolean isEvenWeek) {
        return null;
    }
}

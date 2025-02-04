package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.exception.*;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import java.util.List;

public interface ScheduleService {

    EntityModel<ScheduleApiResponse> importSchedule(ImportDto importDto, String username)
            throws ResourceNotFoundException, ParserException, ParserResponseTimeoutException;

    EntityModel<ScheduleApiResponse> forceImportSchedule(ImportDto importDto, String username)
            throws ParserException, ScheduleNotFoundException;

    EntityModel<ScheduleApiResponse> createSchedule(ScheduleParserResponse saveDto, String username);

    void deleteSchedule(Long id, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    EntityModel<ScheduleApiResponse> deleteScheduleDay(String dayId, String username, boolean isEvenWeek)
            throws ScheduleNotFoundException, StudentNotFoundException;

    EntityModel<ScheduleApiResponse> deleteLesson(String dayOfWeek, Long timeWindowId, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    EntityModel<ScheduleApiResponse> getScheduleById(Long scheduleId) throws ScheduleNotFoundException;

    CollectionModel<EntityModel<ScheduleItem>> getLessonByStudentAndScheduleDayAndTimeWindow(
            String studentName,
            String dayOfWeek,
            Long timeWindowId) throws ScheduleNotFoundException, StudentNotFoundException;

    CollectionModel<EntityModel<ScheduleItem>> getLessonsByStudentAndScheduleDay(String username, String dayOfWeek, boolean isEvenWeek)
            throws ScheduleNotFoundException, StudentNotFoundException;
    EntityModel<ScheduleApiResponse> updateLesson(String dayOfWeek, Long timeWindowId, ScheduleItem scheduleItem, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    EntityModel<ScheduleApiResponse> updateScheduleDay(String dayId, List<ScheduleItem> scheduleItems, String username, boolean isEvenWeek)
            throws ScheduleNotFoundException, StudentNotFoundException;

    EntityModel<ScheduleApiResponse> createScheduleDay(List<ScheduleItem> scheduleItems, String username)
            throws ScheduleNotFoundException, StudentNotFoundException, ResourceExistsException;

    EntityModel<ScheduleApiResponse> getScheduleByStudent(String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    EntityModel<ScheduleApiResponse> createLesson(ScheduleItem scheduleItem, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;
}

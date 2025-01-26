package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.ResourceExistsException;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;
import com.techstud.scheduleuniversity.exception.StudentNotFoundException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import java.util.List;

public interface ScheduleService {

    EntityModel<ScheduleApiResponse> importSchedule(ImportDto importDto, String username)
            throws ScheduleNotFoundException, ParserException;

    EntityModel<ScheduleApiResponse> forceImportSchedule(ImportDto importDto, String username)
            throws ParserException, ScheduleNotFoundException;

    EntityModel<ScheduleApiResponse> createSchedule(ScheduleParserResponse saveDto, String username);

    void deleteSchedule(String id, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    EntityModel<ScheduleApiResponse> deleteScheduleDay(String dayId, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    EntityModel<ScheduleApiResponse> deleteLesson(String scheduleDayId, String timeWindowId, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    EntityModel<ScheduleApiResponse> getScheduleById(String scheduleId) throws ScheduleNotFoundException;

    CollectionModel<EntityModel<ScheduleItem>> getLessonByStudentAndScheduleDayAndTimeWindow(
            String studentName,
            String scheduleDayId,
            String timeWindowId) throws ScheduleNotFoundException, StudentNotFoundException;

    CollectionModel<EntityModel<ScheduleItem>> getLessonsByStudentAndScheduleDay(String username, String scheduleDayId)
            throws ScheduleNotFoundException, StudentNotFoundException;
    EntityModel<ScheduleApiResponse> updateLesson(String scheduleDayId, String timeWindowId, ScheduleItem scheduleItem, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    EntityModel<ScheduleApiResponse> updateScheduleDay(String dayId, List<ScheduleItem> scheduleItems, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    EntityModel<ScheduleApiResponse> createScheduleDay(List<ScheduleItem> scheduleItems, String username)
            throws ScheduleNotFoundException, StudentNotFoundException, ResourceExistsException;

    EntityModel<ScheduleApiResponse> getScheduleByStudent(String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    EntityModel<ScheduleApiResponse> createLesson(ScheduleItem scheduleItem, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;
}

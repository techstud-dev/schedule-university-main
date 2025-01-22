package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleObjectDocument;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleItem;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;

import javax.swing.text.html.parser.Parser;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.exception.StudentNotFoundException;

import java.security.Principal;
import java.util.Date;
import java.util.List;

public interface ScheduleService {

    ScheduleDocument importSchedule(ImportDto importDto, String username)
            throws ScheduleNotFoundException, ParserException;

    ScheduleDocument forceImportSchedule(ImportDto importDto, String username)
            throws ParserException, ScheduleNotFoundException;

    ScheduleDocument createSchedule(ScheduleParserResponse saveDto, String username);

    void deleteSchedule(String id, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    ScheduleDocument deleteScheduleDay(String dayId, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    ScheduleDocument deleteLesson(String scheduleDayId, String timeWindowId, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    ScheduleDocument getScheduleById(String scheduleId) throws ScheduleNotFoundException;

    ScheduleDocument getScheduleByStudentName(String studentName) throws ScheduleNotFoundException, StudentNotFoundException;

    ScheduleDocument updateLesson(String scheduleDayId, String timeWindowId, ScheduleItem scheduleItem, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    ScheduleDocument updateScheduleDay(String dayId, List<ScheduleItem> scheduleItems, String username)
            throws ScheduleNotFoundException, StudentNotFoundException;

    ScheduleDocument saveScheduleDay(List<ScheduleItem> data, String userName) throws IllegalStateException;
}

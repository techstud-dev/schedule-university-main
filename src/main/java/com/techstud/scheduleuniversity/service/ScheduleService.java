package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;

import javax.swing.text.html.parser.Parser;

public interface ScheduleService {

    ScheduleDocument importSchedule(ImportDto importDto, String username) throws ScheduleNotFoundException, ParserException;
    ScheduleDocument forceImportSchedule(ImportDto importDto, String username) throws ParserException, ScheduleNotFoundException;
}

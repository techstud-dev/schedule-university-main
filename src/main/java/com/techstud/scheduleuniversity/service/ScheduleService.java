package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;

public interface ScheduleService {

    ScheduleDocument importSchedule(ImportDto importDto, String username);
    ScheduleDocument createSchedule(ScheduleParserResponse saveDto, String username);
}
